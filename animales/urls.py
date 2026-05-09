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
    path('', include(router.urls)),

    # Rutas para el Login (JWT)
    path('token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),

    # 2. AÑADE ESTA LÍNEA PARA EL REGISTRO
    path('register/', RegistroUsuarioView.as_view(), name='registro_usuario'),
]