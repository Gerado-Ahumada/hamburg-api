# Script para generar 15 registros de juegos para testplayer
# Configuración
$baseUrl = "http://localhost:8080"
$username = "testplayer"
$password = "player123"

Write-Host "Iniciando generación de registros de juegos para testplayer..." -ForegroundColor Green

# Función para hacer login y obtener token
function Get-AuthToken {
    $loginUrl = "http://localhost:8080/api/auth/login"
    $loginBody = @{
        username = "testplayer"
        password = "player123"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri $loginUrl -Method POST -Body $loginBody -ContentType "application/json"
        return $response.token
    } catch {
        Write-Host "Error al obtener token de autenticación: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Función para generar un registro de juego
function Create-GameRecord {
    param(
        [string]$token,
        [string]$userUuid,
        [int]$gameNumber
    )
    
    # Generar fecha aleatoria en los últimos 30 días
    $randomDays = Get-Random -Minimum 0 -Maximum 30
    $randomHours = Get-Random -Minimum 8 -Maximum 22
    $randomMinutes = Get-Random -Minimum 0 -Maximum 59
    $gameDate = (Get-Date).AddDays(-$randomDays).Date.AddHours($randomHours).AddMinutes($randomMinutes)
    
    $gameData = @{
        userUuid = $userUuid
        gameDate = $gameDate.ToString("yyyy-MM-ddTHH:mm:ss")
    } | ConvertTo-Json
    
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/game-activity/register" -Method POST -Body $gameData -Headers $headers
        Write-Host "Juego $gameNumber creado exitosamente - ID: $($response.activityId)" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "Error creando juego $gameNumber : $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# El servidor debería estar corriendo en http://localhost:8080
Write-Host "Asumiendo que el servidor está corriendo en http://localhost:8080" -ForegroundColor Yellow

# Obtener token de autenticación y datos del usuario
$loginResponse = Get-AuthToken
if (-not $loginResponse) {
    Write-Host "No se pudo obtener el token de autenticación. Saliendo..." -ForegroundColor Red
    exit 1
}

# Obtener token y userUuid del login
$loginUrl = "http://localhost:8080/api/auth/login"
$loginBody = @{
    username = "testplayer"
    password = "player123"
} | ConvertTo-Json

$loginData = Invoke-RestMethod -Uri $loginUrl -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginData.token
$userUuid = $loginData.userUuid

Write-Host "Token obtenido exitosamente para testplayer (UUID: $userUuid)" -ForegroundColor Green

# Generar 15 registros de juegos
$successCount = 0
for ($i = 1; $i -le 15; $i++) {
    Write-Host "Creando registro de juego $i/15..." -ForegroundColor Cyan
    if (Create-GameRecord -token $token -userUuid $userUuid -gameNumber $i) {
        $successCount++
    }
    Start-Sleep -Milliseconds 500  # Pausa pequeña entre requests
}

Write-Host "`n=== RESUMEN ===" -ForegroundColor Yellow
Write-Host "Registros creados exitosamente: $successCount/15" -ForegroundColor Green
Write-Host "Registros fallidos: $(15 - $successCount)/15" -ForegroundColor Red

if ($successCount -eq 15) {
    Write-Host "`n¡Todos los registros de juegos fueron creados exitosamente para testplayer!" -ForegroundColor Green
} else {
    Write-Host "`nAlgunos registros fallaron. Revisa los errores arriba." -ForegroundColor Yellow
}

Write-Host "`nAhora puedes probar el dashboard con datos del usuario testplayer en:" -ForegroundColor Cyan
Write-Host "GET $baseUrl/api/user/dashboard" -ForegroundColor White
Write-Host "Authorization: Bearer [token_de_testplayer]" -ForegroundColor Gray