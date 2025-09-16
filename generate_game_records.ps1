# Script para generar 15 registros de juegos para admin
# Configuración
$baseUrl = "http://localhost:8080"
$username = "admin"
$password = "admin"

Write-Host "Iniciando generación de registros de juegos..." -ForegroundColor Green

# Función para hacer login y obtener token
function Get-AuthToken {
    $loginData = @{
        username = $username
        password = $password
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
        return $response.token
    }
    catch {
        Write-Host "Error en login: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Función para generar un registro de juego
function Create-GameRecord {
    param(
        [string]$token,
        [int]$gameNumber
    )
    
    $gameData = @{
        score = Get-Random -Minimum 100 -Maximum 1000
        level = Get-Random -Minimum 1 -Maximum 10
        duration = Get-Random -Minimum 30 -Maximum 300
        gameMode = @("CLASSIC", "ARCADE", "CHALLENGE")[$(Get-Random -Maximum 3)]
    } | ConvertTo-Json
    
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/game-activity/register" -Method POST -Body $gameData -Headers $headers
        Write-Host "Juego $gameNumber creado exitosamente - Score: $($response.score)" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "Error creando juego $gameNumber : $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Verificar si el servidor está corriendo
try {
    $testResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/test" -Method GET -TimeoutSec 5
    Write-Host "Servidor disponible: $testResponse" -ForegroundColor Green
}
catch {
    Write-Host "Error: El servidor no está disponible en $baseUrl" -ForegroundColor Red
    Write-Host "Por favor, asegúrate de que el servidor Spring Boot esté corriendo." -ForegroundColor Yellow
    exit 1
}

# Obtener token de autenticación
$token = Get-AuthToken
if (-not $token) {
    Write-Host "No se pudo obtener el token de autenticación. Saliendo..." -ForegroundColor Red
    exit 1
}

Write-Host "Token obtenido exitosamente" -ForegroundColor Green

# Generar 15 registros de juegos
$successCount = 0
for ($i = 1; $i -le 15; $i++) {
    Write-Host "Creando registro de juego $i/15..." -ForegroundColor Cyan
    if (Create-GameRecord -token $token -gameNumber $i) {
        $successCount++
    }
    Start-Sleep -Milliseconds 500  # Pausa pequeña entre requests
}

Write-Host "`n=== RESUMEN ===" -ForegroundColor Yellow
Write-Host "Registros creados exitosamente: $successCount/15" -ForegroundColor Green
Write-Host "Registros fallidos: $(15 - $successCount)/15" -ForegroundColor Red

if ($successCount -eq 15) {
    Write-Host "`n¡Todos los registros de juegos fueron creados exitosamente!" -ForegroundColor Green
} else {
    Write-Host "`nAlgunos registros fallaron. Revisa los errores arriba." -ForegroundColor Yellow
}