package com.devhonk.grv;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//import java.awt.*;


public class Main extends Application {

    public static Gson gson = new Gson();

    List<JsonObject> repoArrayJava = new ArrayList<>();
    List<String> commitsUrl = new ArrayList<>();

    Parser parser = Parser.builder().build();

    // Create a WebView
    WebView browser = new WebView();

    // Get WebEngine via WebView
    WebEngine webEngine = browser.getEngine();

    static JsonObject followedUsers = new JsonObject();

    Stage primaryStage;


    String username = "<nothing>";
    private List<JsonObject> commitsArrayJava = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        //System.setOut(new PrintStream(new File("cursed.txt")));
        if (new File("followed.json").exists()) {
            BufferedReader bi = null;
            try {
                bi = new BufferedReader(new FileReader("followed.json"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int r = 0;
            String s = "";
            while (true) {
                try {
                    if (!((r = bi.read()) >= 0)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                s += (char) r;
            }
            followedUsers = gson.fromJson(s, JsonObject.class);
        } else {
            try {
                new File("followed.json").createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter("followed.json"));
                bw.write("{\"users\": []}");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            followedUsers.add("users", new JsonArray());
        }
        launch(args);
    }

    @FXML
    public TextField pseudo;

    @FXML
    public ListView repositories;

    @FXML
    public ListView commits;

    @FXML
    public VBox vBox;

    @FXML
    public ProgressBar progress;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setOnCloseRequest(event -> {
            exit();
        });

        Parent root = FXMLLoader.load(getClass().getResource("githubviewer.fxml"));
        primaryStage.setTitle("Github Repo Viewer");
        primaryStage.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/commons/0/04/Vd-Blur2.png"));
        primaryStage.setResizable(false);


        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("dark.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public void pseudoKeyTyped(KeyEvent keyEvent) {

    }

    public void exitApp(ActionEvent actionEvent) {
        exit();
    }

    private void exit() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("followed.json"));
            bw.write(gson.toJson(followedUsers));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0); // Exit with EXIT CODE = 0
    }

    public void pseudoKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (!pseudo.getText().equals(username)) {
                username = pseudo.getText();
                new Thread(() -> {
                    repositories.getItems().clear();
                    JsonArray array = null;
                    try {
                        array = ApiGetter.getRepos(pseudo.getText(), progress);
                    } catch (InexistingUserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    repoArrayJava = JsonArrayToArrayList.jsonArrayToArrayList(array);
                    List<String> arrayJavaNames = new ArrayList<>();
                    for (int i = 0; i < repoArrayJava.size(); i++) {
                        String text = repoArrayJava.get(i).get("name") instanceof JsonNull ? "null" : repoArrayJava.get(i).get("name").getAsString();
                        if (text.length() > 128) {
                            text = text.substring(0, 128 - 3) + "...";
                        }
                        arrayJavaNames.add(i, text);
                    }
                    repositories.getItems().addAll(arrayJavaNames);
                }).start();

            }
        }
    }

    public void pseudoKeyReleased(KeyEvent keyEvent) {
    }

    public void repositoriesClicked(MouseEvent mouseEvent) {
        new Thread(() -> {
            int index = repositories.getSelectionModel().getSelectedIndex();
            List<String> arrayJavaNames = new ArrayList<>();
            if ((!username.equals("<nothing>") && (index >= 0)) && mouseEvent.getButton() == MouseButton.PRIMARY) {
                Object repoName = repositories.getItems().get(index);

                commitsUrl.clear();
                commits.getItems().clear();
                JsonArray commits = null;
                try {
                    commits = ApiGetter.getCommits(username, (String) repoName);
                } catch (InexistingUserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                commitsArrayJava = JsonArrayToArrayList.jsonArrayToArrayList(commits);

                for (int i = 0; i < commitsArrayJava.size(); i++) {
                    String text = commitsArrayJava.get(i).get("commit").getAsJsonObject().get("message") instanceof JsonNull ? "null" : commitsArrayJava.get(i).get("commit").getAsJsonObject().get("message").getAsString();

                    for (int j = 0; j < text.length(); j++) {
                        if (text.charAt(j) == '\n') {
                            text = text.substring(0, j) + "...";
                            break;
                        }
                    }
                    arrayJavaNames.add(i, text);
                    commitsUrl.add(commitsArrayJava.get(i).get("html_url").getAsString());
                }
            }
            if (mouseEvent.isControlDown()) {

            } else {
                this.commits.getItems().addAll(arrayJavaNames);
            }
        }).start();

    }

    public void commitsClick(MouseEvent mouseEvent) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                int index = commits.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    Desktop.getDesktop().browse(new URI(commitsUrl.get(index)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void repoDescriptionAction(ActionEvent actionEvent) {
        int index = repositories.getSelectionModel().getSelectedIndex();


        if (index > -1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("dark.css").toExternalForm());

            dialogPane.getStyleClass().add("root");

            ImageView userAvatar = new ImageView(new Image(repoArrayJava.get(index).get("owner").getAsJsonObject().get("avatar_url").getAsString()));
            userAvatar.getStyleClass().add("avatar");
            userAvatar.setFitWidth(128);
            userAvatar.setFitHeight(128);
            userAvatar.setX(10);
            userAvatar.setY(10);
            alert.setGraphic(userAvatar);

            alert.setTitle("Information about : " + repositories.getItems().get(repositories.getSelectionModel().getSelectedIndex()));
            //alert.setHeaderText();
            alert.setContentText("Author : " + repoArrayJava.get(repositories.getSelectionModel().getSelectedIndex()).get("owner").getAsJsonObject().get("login").getAsString() + "\n" +
                    "Description : " + (repoArrayJava.get(repositories.getSelectionModel().getSelectedIndex()).get("description") instanceof JsonNull ? "Undefined" : repoArrayJava.get(repositories.getSelectionModel().getSelectedIndex()).get("description").getAsString()) + "\n" +
                    "Language : " + (repoArrayJava.get(repositories.getSelectionModel().getSelectedIndex()).get("language") instanceof JsonNull ? "Undefined" : repoArrayJava.get(repositories.getSelectionModel().getSelectedIndex()).get("language").getAsString()) + "\n" +
                    "License : " + ((repoArrayJava.get(repositories.getSelectionModel().getSelectedIndex()).get("license") instanceof JsonNull ? "Undefined" : repoArrayJava.get(repositories.getSelectionModel().getSelectedIndex()).get("license").getAsJsonObject().get("name").getAsString())));
            new Thread(() -> {
                if (pseudo.getText().toLowerCase().equals("chaika9")) {

                    int i = 0;
                    boolean sayThePray = true;
                    while (!userAvatar.isPressed()) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (i >= 3000) {//5 s
                            sayThePray = false;
                            break;
                        }
                        i++;

                    }
                    //stuff
                    if (sayThePray) {
                        Platform.runLater(() -> {
                            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                            alert1.setTitle("chaika9");
                            //alert.setHeaderText();
                            alert1.setContentText("Pray HIM, NOW !");

                            DialogPane dialogPane1 = alert1.getDialogPane();
                            dialogPane1.getStylesheets().add(getClass().getResource("dark.css").toExternalForm());

                            dialogPane1.getStyleClass().add("root");
                            alert1.setGraphic(new ImageView(new Image(getClass().getResource("chaika9.png").toString())));
                            alert1.showAndWait();
                        });
                    }
                }
            }).start();
            alert.showAndWait();
        }
    }

    public void repoReadmeAction(ActionEvent actionEvent) {

        int index = repositories.getSelectionModel().getSelectedIndex();
        Node readme = parser.parse(ApiGetter.getReadMe(username, (String) repositories.getItems().get(index)));
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlReadme = renderer.render(readme);

        webEngine.loadContent(htmlReadme);
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogVbox = new VBox(20);
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialogScene.getStylesheets().add(getClass().getResource("dark.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
        dialogVbox.getChildren().add(browser);
    }

    public void commitDescriptionAction(ActionEvent actionEvent) {

    }

    public void vBoxKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.isControlDown() && keyEvent.isAltDown()) {
            switch (keyEvent.getText().toUpperCase()) {
                case "L":
                    final Stage dialog = new Stage();

                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVBox = new VBox(1);


                    Button addUser = new Button("Add User");

                    addUser.setMaxSize(122, 25);
                    addUser.setTranslateX(206);
                    addUser.setTranslateY(97);

                    Button deleteUser = new Button("Delete User");
                    deleteUser.setMaxSize(122, 25);
                    deleteUser.setTranslateX(206);
                    deleteUser.setTranslateY(97 + 25);

                    Button selectUser = new Button("Select User");
                    selectUser.setMaxSize(122, 25);
                    selectUser.setTranslateX(206);
                    selectUser.setTranslateY(97 + 25 + 25);

                    ListView users = new ListView();

                    users.getStyleClass().add("list");

                    users.setMaxSize(175, 336);
                    users.setTranslateX(31);
                    users.setTranslateY(-32);
                    ArrayList<String> names = new ArrayList<>();

                    JsonArray follows = followedUsers.getAsJsonArray("users");

                    for (int i = 0; i < follows.size(); i++) {
                        names.add(follows.get(i).getAsString());
                    }
                    users.getItems().addAll(names);

                    dialogVBox.getChildren().addAll(addUser, deleteUser, selectUser, users);

                    Scene dialogScene = new Scene(dialogVBox, 335, 367);
                    dialogScene.getStylesheets().add(getClass().getResource("dark.css").toExternalForm());

                    dialog.setScene(dialogScene);
                    dialog.show();


                    deleteUser.setOnAction((e) -> {
                        int index = users.getSelectionModel().getSelectedIndex();
                        if (index >= 0) {
                            users.getItems().clear();
                            names.clear();
                            follows.remove(index);
                            for (int i = 0; i < follows.size(); i++) {
                                String follow = follows.get(i).getAsString();
                                names.add(follow);
                            }
                            users.getItems().addAll(names);
                        }
                    });

                    addUser.setOnAction((e) -> {
                        TextInputDialog name = new TextInputDialog();
                        name.setTitle("Enter an username here...");
                        name.setHeaderText("Enter an username here...");
                        name.setContentText("Username: ");

                        Optional<String> result = name.showAndWait();

                        result.ifPresent(nameStr -> {
                            followedUsers.get("users").getAsJsonArray().add(nameStr);
                            users.getItems().clear();
                            names.clear();
                            for (int i = 0; i < follows.size(); i++) {
                                String follow = follows.get(i).getAsString();
                                names.add(follow);
                            }
                            users.getItems().addAll(names);
                        });
                    });
                    selectUser.setOnAction((e) -> {
                        int index = users.getSelectionModel().getSelectedIndex();
                        if (index >= 0) {
                            pseudo.setText(follows.get(index).getAsString());
                        }
                    });


                    break;
            }
        }
    }
}
