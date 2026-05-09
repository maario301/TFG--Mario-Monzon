from rest_framework import viewsets, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import AllowAny
from django.contrib.auth.models import User
from .models import Animal, Consulta
from .serializers import AnimalSerializer, ConsultaSerializer

# Tus ViewSets actuales (déjalos como están)
class AnimalViewSet(viewsets.ModelViewSet):
    queryset = Animal.objects.all()
    serializer_class = AnimalSerializer

class ConsultaViewSet(viewsets.ModelViewSet):
    queryset = Consulta.objects.all()
    serializer_class = ConsultaSerializer

# --- AÑADE ESTO PARA EL REGISTRO ---
class RegistroUsuarioView(APIView):
    permission_classes = [AllowAny]  # Importante: permite que alguien sin cuenta pueda registrarse

    def post(self, request):
        username = request.data.get('username')
        password = request.data.get('password')

        if not username or not password:
            return Response({'error': 'Faltan datos'}, status=status.HTTP_400_BAD_REQUEST)

        if User.objects.filter(username=username).exists():
            return Response({'error': 'Este usuario ya existe'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            # Crea el usuario en la tabla de usuarios de Django
            User.objects.create_user(username=username, password=password)
            return Response({'mensaje': 'Usuario creado correctamente'}, status=status.HTTP_201_CREATED)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)