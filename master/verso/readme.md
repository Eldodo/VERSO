# Sommaire
## 1. Importer verso dans Eclipse
## 2. Intégrer JavaFX dans Eclipse


-------------------------------------------


# 1.Importer verso dans Eclipse

## 1.1 Cloner le répertoire git dans votre workspace.
Ouvrir la vue Project Explorer.
>	Window --> Show View --> Other --> General --> Package Explorer

Dans la vue *Package Explorer* faire un clic droit puis séléctionner *Import*.

Dans la fenêtre qui vient d'apparaître, sélectionner *Git --> Projects from Git*.

Sélectionner *Clone URI*.

Entrer l'URI .git du projet et s'authentifier à l'aide de son compte BitBucket. (L'URI est trouvable sur https://bitbucket.org/batotedo/verso/src/master/ , bouton "Clone").

Dans *Wizard for project import*, sélectionner *Import existing Eclipse projects*.

Dans les projets qui apparaissent, désélectionner *SimpleVersoParser* afin de ne laisser que *verso*.


## 1.2 Exécuter le projet
Ouvrir le fichier Main.java
>	verso > verso/src > verso > Main.java

Aller dans *Run configuration* (il est possible de devoir éxecuter le Main une première fois pour qu'il apparaisse), onglet *Arguments* ajouter dans la case *VM arguments*
>	-Djava.util.logging.config.file=verso/lib/logging.properties
>	-Djava.library.path="./verso/lib/3d/libwinamd64"
>	-Xms256m -Xmx8g


A noter que le library path dépend de votre OS. Plusieurs options s'offrent à vous :

* Linux 64bits : *liblinuxamd64*

* Linux 32bits : *liblinuxi586*

* Mac OS         : *libMac*

* Windows 64bits : *libwinamd64*

* Windows 32bits : *libwini586*


A vous de faire le choix adéquat. Toutes ces bibliothèques sont disponibles dans le répertoire *verso/lib/3d* et permettent l'affichage de l'interface graphique.

*Vous pouvez désormais ***lancer le projet*** sous Eclipse sans problème.*


## 1.3 Télécharger les dépendances
Afin que verso puisse fonctionner plainement, vous aurez besoin de ces différentes applications :

 * Subclipse - disponible depuis eclipse, nécessaire pour compiler le parser
 * Graphviz - Graph Visualization Software : https://www.graphviz.org/download/


# 2. Intégrer JavaFX dans Eclipse


Ouvrir Eclipse.

Allez sur http://marketplace.eclipse.org/content/efxclipse et glissez-déposez le bouton install dans Eclipse pour installer le Plugin.

Ou bien alors recherchez *e(fx)clipse* directement dans Eclipse
>	Help > Eclipse MarketPlace...


