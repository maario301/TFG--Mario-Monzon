from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
# 1. AÑADE ESTOS IMPORTS PARA SWAGGER
from rest_framework import permissions
from drf_yasg.views import get_schema_view
from drf_yasg import openapi

# 2. CONFIGURA EL SCHEMA_VIEW AQUÍ
schema_view = get_schema_view(
   openapi.Info(
      title="VenoMap API",
      default_version='v1',
      description="Documentación de la API de VenoMap",
   ),
   public=True,
   permission_classes=(permissions.AllowAny,),
)

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include('animales.urls')),
    path('api-auth/', include('rest_framework.urls')),
    path('api/swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='swagger'),  # <-- AÑADE ESTO
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT) # AÑADE ESTO