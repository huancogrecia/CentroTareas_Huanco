# CentroTareas_Huanco

## Observación del Paso 1

Al pulsar «Tarea larga (MAL)», la pantalla deja de responder durante unos cuatro segundos. En ese intervalo el contador «Tócame» no se actualiza porque la espera ocupa el hilo principal, que también atiende los toques y dibuja la pantalla.

«Trabajando...» se guarda en el estado, pero Compose no alcanza a dibujarlo durante la pausa. Cuando el hilo principal vuelve a estar libre, el estado ya dice «Terminó». Si la espera durara diez segundos y trato de interactuar con la pantalla, Android podría mostrar un aviso de que la aplicación no responde (ANR).
