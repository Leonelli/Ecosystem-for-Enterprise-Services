### Mobility Print - Desktop client (Windows, Mac OS) - Windows
- FBK [distribuisce](https://print.fbk.eu:9164/help/mobility-print-devices/topics/en/client-setup.html) un file eseguibile che in fase di installazione richiede le credenziali aziendali (nome utente e password FBK) e le stampanti (FBK) configurate per Mobility Print da rendere disponibili nel sistema operativo.
- Nelle proprietà della stampante non sono presenti le credenziali FBK, ma l'URL associato alla porta di stampa include il nome utente (FBK) e un numero esadecimale di 33 caratteri.
	- Verificare se occorre re-inserire le credenziali dopo un certo numero di giorni.

### Mobility Print - Mobile App (Android, iOS, Chrome OS) - Android
- Occorre scaricare l'applicazione mobile [Mobility Print] (https://play.google.com/store/apps/details?id=com.papercut.projectbanksia) e, una volta avviata, verificare di aver dato il pemesso di esecuzione in background e quello di ricezione notifiche.
- Se collegati via VPN, quando viene inviato qualcosa in stampa compaiono le stampanti (FBK) configurate per Mobility Print.
- Dopo aver selezionato la stampante, una notifica push avvisa della necessità di autenticarsi con credenziali aziendali (nome utente e password FBK).
	- E' possibile memorizzare le credenziali per 7 giorni. 

### Portale Web PaperCut NG
Dal [portale web](https://print.fbk.eu:9192/app?service=page/UserSummary) di PaperCut, una volta autenticati con  credenziali aziendali (nome utente e password FBK) è possibile vedere i seguenti metadati per ogni lavoro di stampa:
- Data (popolato)
- Nome utente (popolato)
- Nome completo (popolato)
- Ufficio dell'utente
- Sezione dell'utente
- E-mail (popolato)
- Numero carta
- Account addebitato (popolato - "USER")
- Nome account 
- Codice account 
- Nome account 
- Codice account 
- Sottonome dell'account 
- Sottocodice dell'account 
- Server della stampante (popolato - "ntprint-vm")
- Nome della stampante (popolato - e.s., "nord.pp.colorcopier")
- Identificatore stampante reale (popolato e.s., "net://192.168.124.91")
- Tipo/modello stampante (popolato e.s., "TOSHIBA e-STUDIO4555C")
- Numero seriale della stampante popolato e.s., "C7DD59672")
- Documento (popolato e.s., "Microsoft Word - prova")
- Tipo di utente (popolato e.s., "PRINT")
- Pagine totali (popolato - e.s., "1")
- Totale delle pagine a colori (popolato - e.s., "1" anche se all'invio era B/W)
- Stima delle pagine a colori (popolato - e.s., "Y")
- Copie (popolato - e.s., "1")
- Costo (popolato - e.s., "0,00")
- Dimensione del foglio (popolato - e.s., "A4")
- Larghezza del foglio (mm) (popolato - e.s., "297")
- Altezza del foglio (mm) (popolato - e.s., "210")
- Fronte retro (popolato - e.s., "N")
- Bianco e nero (popolato - e.s., "N")
- Addebitato (popolato - e.s., "Y")
- Client (popolato con il tipo e full device name. E.s., "desktop - zod.fbk.eu")
- Dimensione (Kb) (popolato - e.s., "2")
- Lingua della stampante (popolato - e.s., "PCL6")
- Commenti
- Stampate (popolato - e.s., "Y")
- Annullato (popolato - e.s., "N")
- Rimborsato (popolato - e.s., "NOT_REFUNDED")
- Consentito (popolato - e.s., "Y")
- Motivo del blocco
- Archiviati (popolato - e.s., "N")
- Spenta (popolato - e.s., "N")
- pagine fronte retro (popolato - e.s., "0")
- pagine singole (popolato - e.s., "1")
