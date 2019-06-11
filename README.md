# VERSO

Ce répertoire comprend les sources de verso (https://github.com/Eldodo/VERSO/tree/master/master/verso/verso/src/verso) et les sources de l'outil pour récupérer les traces de la jvm (https://github.com/Eldodo/VERSO/tree/master/master/ExecTracerWin/, qui compile vers un dll windows, ExecTracer est une ancienne version développée à l'université il y a quelques années et probablement qu'un étudiant avait utilisé pour VERSO, mais le code n'est plus à jour). Le dll compilé utilisable avec la jvm est présent ici https://github.com/Eldodo/VERSO/tree/master/master/ExecTracerWin/bin/Debug.

Le dossier eclipse à la racine contient une version d'eclipse avec toutes les dépendances pour compiler VERSO. pinboard.jar contient un mini logiciel de dessin que j'avais développé pour un cours, dont je me suis servi pour mes tests, le fichier pinboard_trace.txt est un fichier de trace correspondant à un petit scénario d'utilisation du logiciel de dessin

Les classes que j'ai créées/modifiée dans VERSO sont:
  - la classe tracesUtils dans le paquetage traces. Les classes Node et DynamicTracesGraph ne sont pas utilisées. La classe CIELab n'est pas de ma création, l'entête de la classe contient les crédits/
  - les classes SystemRepresentation, SceneLandscape et Main (ainsi que quelques autres classes) ont été modifiées pour supporter l'affichage des traces.
  
  Pour lancer VERSO : une fois le projet ouvert dans eclipse, il faut suivre les instructions dans le readme présent dans le répertoire https://github.com/Eldodo/VERSO/tree/master/master/verso, section 1.2 exécuter le projet, et avec l'option -s pinboard pour le lancer avec le mini logiciel de dessin. Une fois VERSO lancé, aller dans fichier->ajouter un fichier de traces et sélectionner un fichier de traces fait avec le logiciel chargé. Une fois le fichier chargé, la touche 't' permet d'afficher la carte de chaleur et la touche 'u' permet d'afficher les liens d'appels.
  
