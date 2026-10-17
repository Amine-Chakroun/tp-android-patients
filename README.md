1. Pourquoi la liste n’est-elle pas perdue quand on tourne l’écran ?

Quand on tourne le téléphone, Android détruit l’écran et le recrée. Mais le ViewModel, lui, 
reste en vie, et c’est lui qui garde la liste en mémoire. Le chargement des patients se fait dans init, donc seulement quand le ViewModel est créé. Après la rotation, l’écran se reconnecte simplement au même ViewModel et retrouve la liste telle quelle, sans refaire de requête.

2. Pourquoi _uiState est privé et modifiable, alors que uiState est public et en lecture seule ?

Pour que seul le ViewModel puisse changer l’état de l’écran. L’interface peut le regarder et prévenir le ViewModel quand l’utilisateur fait quelque chose (un clic sur supprimer, par exemple), mais elle ne touche jamais directement à la liste. Comme ça, il n’y a qu’un seul endroit où l’état change, et si un bug apparaît, on sait où chercher.

3. Quelle est la différence entre un état et un événement ?

Un état (StateFlow), c’est ce que l’écran doit montrer en ce moment : il a toujours une valeur, et quand l’écran se recrée, il la récupère. Dans le TP, c’est uiState : chargement, liste ou erreur.

Un événement (SharedFlow), c’est quelque chose qui arrive une fois puis disparaît. Dans le TP, c’est messages, qui envoie « Ali ajouté » à la Snackbar. Si on utilisait un état à la place, le message réapparaîtrait à chaque rotation, ce qui serait gênant.

4. Que se passe-t-il si age est un Int non nullable et que l’API renvoie un patient sans âge ?

Kotlin ne sait pas quoi mettre dans age, puisque la valeur n’existe pas dans le JSON. La conversion plante avec une SerializationException. Le Repository l’attrape et l’application affiche « Réponse inattendue du serveur », alors que le serveur a bien répondu. C’est pour ça qu’on écrit val age: Int? = null : l’âge devient facultatif.

5. Quelle différence entre HttpException et IOException ?

HttpException : le serveur a répondu, mais pour dire qu’il y a un problème (code 4xx ou 5xx).
IOException : le serveur n’a pas répondu du tout (éteint, mauvaise adresse, pas de réseau).

Si le serveur est arrêté, on obtient une IOException, et l’app affiche « Serveur injoignable ». Si le patient n’existe pas, le serveur répond bien, mais avec un 404 : c’est donc une HttpException, et l’app affiche « Patient introuvable ».

6. Si on remplaçait l’API par une base Room, qu’est-ce qui changerait ?

Ce qui change, c’est tout ce qui concerne la source des données :

PatientApi.kt serait remplacé par un DAO Room (avec une entité et une classe Database) ;
PatientRepository.kt appellerait le DAO au lieu de Retrofit ;
AppContainer.kt construirait la base Room au lieu de Retrofit ;
les dépendances Gradle seraient ajustées.

Ce qui ne change pas : le ViewModel, l’état de l’écran (PatientsUiState), tous les composables et la classe Application. Ils ne savent pas d’où viennent les données, ils parlent seulement au Repository. C’est tout l’intérêt de séparer en couches.
<img width="990" height="2049" alt="8528631a-bc29-4a99-b775-17b7d16b0cab" src="https://github.com/user-attachments/assets/eb25c790-106f-42e3-afeb-533f63fb3590" />
<img width="986" height="2045" alt="a223ed31-2128-4ca9-9d54-c0b1d5e4b1fd" src="https://github.com/user-attachments/assets/5588c58f-e409-47eb-9af7-fc0285543d01" />
<img width="988" height="2049" alt="6a07c656-9002-4c62-a47d-ffac3cebbe08" src="https://github.com/user-attachments/assets/0b3c73fd-cff5-484e-8bcb-d6f4140a5358" />
<img width="1486" height="492" alt="logcat" src="https://github.com/user-attachments/assets/d236c337-d9a8-4175-a50a-861cdda7c29f" />
<img width="1534" height="824" alt="mongodb" src="https://github.com/user-attachments/assets/ae36c392-f254-4d86-a737-a2b1a79aa016" />
