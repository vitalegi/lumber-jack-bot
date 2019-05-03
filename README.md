# README

Bot per il gioco Telegram LumberJack (<https://telegram.me/gamebot?game=Lumberjack>).

## Sviluppi futuri

- riconoscere quando la partita è finita
- riconoscere/stimare il punteggio della partita
- avviare una nuova partita
- introdurre IA per la gestione dei parametri di esecuzione
- introdurre IA per la decisione delle azioni da compiere

## Installazione

```cmd
mvn clean package
```

## Esecuzione

```cmd
java -jar target\lumber-jack-bot-0.0.1-SNAPSHOT.jar --fps=9.7 --napIfChange=50 --logImgs=false
```

java -jar target\lumber-jack-bot-0.0.1-SNAPSHOT.jar --fps=10.3 --napIfChange=60 --logImgs=false
best: --fps=10.3 --napIfChange=60