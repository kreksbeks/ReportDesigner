package com.voli.soft

//@Grab('org.codehaus.groovyfx:groovyfx:0.3.1')
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node
import javafx.scene.SnapshotParameters
import javafx.scene.image.ImageView
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.opendolphin.core.client.ClientAttribute;
import org.opendolphin.core.client.ClientDolphin

import static groovyx.javafx.GroovyFX.start

class DesignerView {

    Stage           primaryStage
    ClientDolphin   dolphin
    List<String>    users   = "felicitas florian sophie elin".tokenize(' ')
    List<String>    actions = "wakeup play gotobed".tokenize(' ')
    List<String>    stati   = "asleep awake playing".tokenize(' ')
    ImageView       dragImageView = new ImageView()

    void show(ClientDolphin dolphin) {
        this.dolphin = dolphin
        start { app ->
            createView(delegate)
            primaryStage = delegate.primaryStage
            addToolsDragNDrop()
            createModels()
            binding()
            primaryStage.show()
        }
    }

    private void createView(sceneGraphBuilder) {
        sceneGraphBuilder.stage (title: "Report Designer", width: 512, height: 514, visible: true, resizable: true) {
            scene {
                fxml resource("/designerView.fxml")
            }
        }
    }

    def addToolsDragNDrop() {
        findUIElement('toolBar').children.each { addToolGestures(it, findUIElement('sceneRoot')) }
        initDesignerPane( findUIElement('designerPane') )
    }

    private void createModels() {
        users.each {
            dolphin.presentationModel(it, "user",
                    new ClientAttribute("name", it, "$it-name"),
                    new ClientAttribute("status", "asleep", "$it-status"),
                    new ClientAttribute("wakeup", true, "$it-wakeup-enabled"),
                    new ClientAttribute("play",   false,"$it-play-enabled"),
                    new ClientAttribute("gotobed",false,"$it-gotobed-enabled")
            )
        }
        dolphin.presentationModel("current_user", "user", name:null, status:null, wakeup:false, play:false, gotobed:false)

        for (user in users) {
            for (status in stati) {
                dolphin.presentationModel("${user}-${status}", "Detail", new ClientAttribute('detail','',"${user}-${status}-detail"))
            }
        }
        dolphin.presentationModel("current_detail", "Detail", detail:'')
    }

    private void binding() {
//        def current_user   = dolphin["current_user"]
//        def current_detail = dolphin["current_detail"]
//
//        def update_current_detail = {
//            def current_user_detail = dolphin["${current_user.name.value}-${current_user.status.value}"]
//            if (!current_user_detail) return it // values are null on startup
//            dolphin.apply(current_user_detail).to(current_detail)
//            return it
//        }
//
//        bind "name"   of current_user to FX.TEXT of user_label,   update_current_detail
//        bind "status" of current_user to FX.TEXT of status_label, update_current_detail
//
//        bind "detail" of current_detail to FX.TEXT of detail_label
//        bind "detail" of current_detail to FX.TEXT of detail_textfield
//        bind FX.TEXT  of detail_textfield to "detail" of current_detail
//
//        users.each { user ->
//            def button = this."${user}_button"
//            button.onAction = { dolphin.apply(dolphin[user]).to(current_user) } as EventHandler
//            bind "name" of current_user to "style" of button, { it == user ? "-fx-background-color:transparent" : "" }
//        }
//        actions.each { action ->
//            def button = this."${action}_button"
//            button.onAction = { dolphin.send(action) } as EventHandler
//            bind action of current_user to FX.DISABLE of button, { !it }
//        }
    }



    def findUIElement(String name) {
        primaryStage.scene.lookup("#$name")
    }

    private void addToolGestures(Node toolNode, Node sceneRoot) {
        toolNode.onDragDetected = { MouseEvent e ->
            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.fill = Color.TRANSPARENT;
            dragImageView.image = toolNode.snapshot(snapParams, null);

            sceneRoot.getChildren().add(dragImageView);

            dragImageView.startFullDrag();
            e.consume();
        } as EventHandler

        toolNode.onMouseDragged = { MouseEvent e ->
            Point2D localPoint = sceneRoot.sceneToLocal(new Point2D(e.sceneX, e.sceneY));
            dragImageView.relocate(
                    (int) (localPoint.getX() - dragImageView.boundsInLocal.width / 2),
                    (int) (localPoint.getY() - dragImageView.boundsInLocal.height / 2)
            );
            e.consume();
        } as EventHandler

        toolNode.onMouseEntered = { MouseEvent e ->
            toolNode.setCursor(Cursor.HAND);
        } as EventHandler

        toolNode.onMousePressed = { MouseEvent e ->
//            dragItem = node;
            dragImageView.mouseTransparent = true;
            toolNode.mouseTransparent = true;
            toolNode.cursor = Cursor.CLOSED_HAND;
        } as EventHandler

        toolNode.onMouseReleased = { MouseEvent e ->
//            dragItem = null;
            dragImageView.mouseTransparent = false;
            toolNode.mouseTransparent = false;
            toolNode.cursor = Cursor.DEFAULT;
            sceneRoot.getChildren().remove(dragImageView);
        } as EventHandler
    }

    private static def initDesignerPane(Node designPane) {
        designPane.onMouseDragEntered = { MouseDragEvent e ->
            designPane.style = "-fx-border-color:red;-fx-border-width:2;-fx-border-style:solid;";
            e.consume();
        } as EventHandler;

        designPane.onMouseDragExited = { MouseDragEvent e ->
            designPane.style = "-fx-border-style:none;";
            e.consume();
        } as EventHandler;

        designPane.onMouseDragReleased = { MouseDragEvent e ->
            designPane.children.add(e.gestureSource)
            e.consume();
        } as EventHandler;
    }
}