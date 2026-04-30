from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import AnimalViewSet, ConsultaViewSet

router = DefaultRouter()
router.register(r'animales', AnimalViewSet)
router.register(r'consultas', ConsultaViewSet)

from django.urls import path
from . import views
# Importamos las vistas de la librería JWT
from rest_framework_simplejwt.views import (
    TokenObtainPairView,
    TokenRefreshView,
)

urlpatterns = [
    # Ruta para ver los animales
    path('animales/', views.lista_animales, name='lista_animales'),
    
    # Rutas para el Login (JWT)
    path('token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
]