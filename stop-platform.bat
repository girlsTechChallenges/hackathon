@echo off
REM Script para parar toda a plataforma Health Tech
REM Windows

echo ============================================
echo   Health Tech Platform - Shutdown Script
echo ============================================
echo.

REM Menu de opções
echo Como deseja parar a plataforma?
echo.
echo 1^) Parar containers ^(mantém dados^)
echo 2^) Parar e remover containers ^(mantém volumes/dados^)
echo 3^) Parar e APAGAR TUDO ^(remove containers e dados^)
echo.
set /p option="Escolha uma opção [1-3]: "

if "%option%"=="1" (
    echo.
    echo Parando containers...
    docker-compose stop
    echo [92m✓ Containers parados[0m
    echo Para reiniciar: docker-compose start
) else if "%option%"=="2" (
    echo.
    echo Parando e removendo containers...
    docker-compose down
    echo [92m✓ Containers removidos ^(dados preservados^)[0m
    echo Para reiniciar: docker-compose up -d
) else if "%option%"=="3" (
    echo.
    echo [91m⚠ ATENÇÃO: Isso vai apagar TODOS os dados dos bancos![0m
    set /p confirm="Tem certeza? (digite 'SIM' para confirmar): "
    
    if "!confirm!"=="SIM" (
        echo.
        echo Parando e removendo tudo...
        docker-compose down -v
        echo [92m✓ Tudo removido ^(incluindo dados^)[0m
        echo Para reiniciar do zero: docker-compose up -d --build
    ) else (
        echo [93mOperação cancelada[0m
    )
) else (
    echo [91mOpção inválida![0m
    exit /b 1
)

echo.
echo Para verificar status: docker-compose ps
echo.
