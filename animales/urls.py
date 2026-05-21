from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from .views import AnimalViewSet, ConsultaViewSet, RegistroUsuarioView, GuardarAvistamientoView, ListarAvistamientosView, MeView

router = DefaultRouter()
router.register(r'animales', AnimalViewSet)
router.register(r'consultas', ConsultaViewSet)

urlpatterns = [
    path('register/', RegistroUsuarioView.as_view(), name='registro_usuario'),
    path('token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('avistamientos/', ListarAvistamientosView.as_view(), name='listar_avistamientos'),
    path('avistamientos/guardar/', GuardarAvistamientoView.as_view(), name='guardar_avistamiento'),
    path('me/', MeView.as_view(), name='me'),
    path('', include(router.urls)),
]