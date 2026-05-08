from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    # La ruta del panel de control que ya tienes
    path('admin/', admin.site.urls),

    # Esta es la línea maestra para los animales
    path('api/', include('animales.urls')),

    # AÑADE ESTA LÍNEA AQUÍ ABAJO:
    path('api-auth/', include('rest_framework.urls')),
]