from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include('animales.urls')),
    path('api-auth/', include('rest_framework.urls')),
    path('api/swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='swagger'),  # <-- AÑADE ESTO
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT) # AÑADE ESTO