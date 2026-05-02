from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import AnimalViewSet, ConsultaViewSet
from rest_framework_simplejwt.views import (
    TokenObtainPairView,
    TokenRefreshView,
)

# 1. El router se encarga de la ruta 'animales/' automáticamente usando las clases
router = DefaultRouter()
router.register(r'animales', AnimalViewSet)
router.register(r'consultas', ConsultaViewSet)

urlpatterns = [
    # Esto incluye automáticamente la ruta 'animales/' que tenías antes,
    # pero conectándola a la clase AnimalViewSet que es la que existe.
    path('', include(router.urls)), 
    
    # Rutas para el Login (JWT) que necesitas para Android
    path('token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
]