from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import AnimalViewSet, ConsultaViewSet

router = DefaultRouter()
router.register(r'animales', AnimalViewSet)
router.register(r'consultas', ConsultaViewSet)

urlpatterns = [
    # AQUÍ NO DEBE HABER NINGÚN include('animales.urls')
    path('', include(router.urls)),
]