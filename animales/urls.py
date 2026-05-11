from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import AnimalViewSet, ConsultaViewSet, RegistroUsuarioView # 1. Importa la nueva vista
from rest_framework_simplejwt.views import (
    TokenObtainPairView,
    TokenRefreshView,
)

router = DefaultRouter()
router.register(r'animales', AnimalViewSet)
router.register(r'consultas', ConsultaViewSet)

urlpatterns = [
    # 1. LA RUTA DE REGISTRO DEBE IR PRIMERO Y SIN "api/" 
    # (Porque el "api/" ya se lo pone el archivo principal)
    path('register/', RegistroUsuarioView.as_view(), name='registro_usuario'),

    # 2. Las rutas del Token
    path('token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),

    # 3. El router al final
    path('', include(router.urls)),
]