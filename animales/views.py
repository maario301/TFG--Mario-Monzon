from rest_framework import viewsets, status, filters
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import AllowAny, IsAuthenticated, IsAdminUser
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth.models import User
from django.conf import settings
from google.oauth2 import id_token as google_id_token
from google.auth.transport import requests as google_requests
from .models import Animal, Consulta
from .serializers import AnimalSerializer, ConsultaSerializer


class AnimalViewSet(viewsets.ModelViewSet):
    queryset = Animal.objects.all()
    serializer_class = AnimalSerializer
    filter_backends = [filters.SearchFilter]
    search_fields = ['nombre_cientifico']

    def get_permissions(self):
        # Solo admin puede crear, editar o eliminar animales
        if self.action in ['create', 'update', 'partial_update', 'destroy']:
            return [IsAdminUser()]
        # Cualquiera puede consultar
        return [IsAuthenticated()]


class ConsultaViewSet(viewsets.ModelViewSet):
    queryset = Consulta.objects.all()
    serializer_class = ConsultaSerializer

    def get_permissions(self):
        # Solo admin puede eliminar marcadores
        if self.action == 'destroy':
            return [IsAdminUser()]
        return [IsAuthenticated()]


class RegistroUsuarioView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        username = request.data.get('username')
        password = request.data.get('password')

        if not username or not password:
            return Response({'error': 'Faltan datos'}, status=status.HTTP_400_BAD_REQUEST)

        if User.objects.filter(username=username).exists():
            return Response({'error': 'Este usuario ya existe'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            User.objects.create_user(username=username, password=password)
            return Response({'mensaje': 'Usuario creado correctamente'}, status=status.HTTP_201_CREATED)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class GuardarAvistamientoView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        animal_nombre = request.data.get('nombre_cientifico')
        latitud = request.data.get('latitud')
        longitud = request.data.get('longitud')

        try:
            animal = Animal.objects.get(nombre_cientifico__iexact=animal_nombre)
        except Animal.DoesNotExist:
            return Response({'error': f'Animal no encontrado: {animal_nombre}'}, status=status.HTTP_404_NOT_FOUND)

        consulta = Consulta.objects.create(
            usuario=request.user,
            animal=animal,
            latitud=latitud,
            longitud=longitud
        )
        return Response(ConsultaSerializer(consulta).data, status=status.HTTP_201_CREATED)
class MeView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        return Response({
            'username': request.user.username,
            'is_staff': request.user.is_staff
        })


class GoogleLoginView(APIView):
    """Recibe el id_token de Google que manda la app, lo verifica con Google,
    crea/recupera el usuario y devuelve un JWT igual que el login normal."""
    permission_classes = [AllowAny]

    def post(self, request):
        token = request.data.get('id_token')
        if not token:
            return Response({'error': 'Falta id_token'}, status=status.HTTP_400_BAD_REQUEST)

        # 1. Verificamos el token contra Google (firma, caducidad y audiencia)
        try:
            info = google_id_token.verify_oauth2_token(
                token, google_requests.Request(), settings.GOOGLE_CLIENT_ID
            )
        except ValueError:
            return Response({'error': 'Token de Google no válido'}, status=status.HTTP_401_UNAUTHORIZED)

        email = info.get('email')
        if not email:
            return Response({'error': 'El token no contiene email'}, status=status.HTTP_400_BAD_REQUEST)

        # 2. Buscamos o creamos el usuario (usamos el email como username)
        user, creado = User.objects.get_or_create(
            username=email,
            defaults={
                'email': email,
                'first_name': info.get('given_name', ''),
                'last_name': info.get('family_name', ''),
            }
        )
        if creado:
            # Cuenta solo de Google: sin contraseña utilizable
            user.set_unusable_password()
            user.save()

        # 3. Emitimos el JWT de siempre (SimpleJWT)
        refresh = RefreshToken.for_user(user)
        return Response({
            'access': str(refresh.access_token),
            'refresh': str(refresh),
        })

class ListarAvistamientosView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        consultas = Consulta.objects.filter(
            latitud__isnull=False,
            longitud__isnull=False
        ).select_related('animal', 'usuario')

        data = []
        for c in consultas:
            data.append({
                'id': c.id,
                'nombre_comun': c.animal.nombre_comun,
                'nombre_cientifico': c.animal.nombre_cientifico,
                'usuario': c.usuario.username,
                'fecha': c.fecha.strftime('%d/%m/%Y %H:%M'),
                'latitud': c.latitud,
                'longitud': c.longitud,
            })
        return Response(data)
    