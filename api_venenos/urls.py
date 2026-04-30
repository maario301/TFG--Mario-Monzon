from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    # La ruta del panel de control que ya tienes
    path('admin/', admin.site.urls),
    
    # Esta es la línea maestra: redirige todo lo que sea 'api/...' 
    # al archivo urls.py de tu carpeta 'animales'
    path('api/', include('animales.urls')), 
]