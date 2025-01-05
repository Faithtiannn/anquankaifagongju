package com.example.test5;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloApplication extends Application {

    @FXML
    private TextField url;
    @FXML
    private TextArea batchUrls;
    @FXML
    private TextArea textAreaVulnerability;
    @FXML
    private TextArea textAreaCommandExecution;
    @FXML
    private TextArea textAreaBatchCommandExecution; // 添加声明
    @FXML
    private ComboBox<String> pocSelector;
    @FXML
    private ComboBox<String> commandSelector;
    @FXML
    private Button batchCheckButton;
    @FXML
    private Button fileSelectButton;
    @FXML
    private Button batchCommandButton; // 添加声明
    private TextField commandInput;

    private VulnerabilityChecker vulnerabilityChecker;
    private ExecutorService executorService;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);

        vulnerabilityChecker = new VulnerabilityChecker();
        executorService = Executors.newFixedThreadPool(10);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(15);
        gridPane.setPadding(new Insets(15));

        // 单目标检测区域
        Text singleTargetTitle = new Text("单目标检测");
        singleTargetTitle.setFont(Font.font("Arial", 16));
        gridPane.add(singleTargetTitle, 0, 0, 4, 1);

        // URL输入
        Label urlLabel = new Label("目标URL:");
        urlLabel.setFont(Font.font("Arial", 14));
        url = new TextField();
        url.setPrefWidth(400);
        url.setPromptText("请输入目标URL");
        gridPane.add(urlLabel, 0, 1);
        gridPane.add(url, 1, 1, 2, 1);

        // 单目标检测按钮
        Button singleCheckButton = new Button("开始检测");
        singleCheckButton.setStyle("-fx-base: #4CAF50;");
        singleCheckButton.setOnAction(this::handleSingleCheck);
        gridPane.add(singleCheckButton, 3, 1); // 放在目标URL输入框的右侧

        // PoC选择
        Label pocLabel = new Label("漏洞类型:");
        pocLabel.setFont(Font.font("Arial", 14));
        pocSelector = new ComboBox<>();
        ObservableList<String> pocOptions = FXCollections.observableArrayList(
                "thinkphp5",
                "thinkphp5.0.23");
        pocSelector.setItems(pocOptions);
        pocSelector.setValue("thinkphp5");
        gridPane.add(pocLabel, 0, 2);
        gridPane.add(pocSelector, 1, 2);

        // 分隔线
        Separator separator1 = new Separator();
        gridPane.add(separator1, 0, 3, 4, 1);

        // 批量检测区域
        Text batchTitle = new Text("批量检测");
        batchTitle.setFont(Font.font("Arial", 16));
        gridPane.add(batchTitle, 0, 4, 4, 1);

        // 批量URL输入
        Label batchUrlLabel = new Label("URL列表:");
        batchUrlLabel.setFont(Font.font("Arial", 14));
        batchUrls = new TextArea();
        batchUrls.setPrefRowCount(5);
        batchUrls.setPromptText("每行输入一个URL");
        gridPane.add(batchUrlLabel, 0, 5);
        gridPane.add(batchUrls, 1, 5, 2, 1);

        // 批量操作按钮
        HBox batchButtons = new HBox(10);
        fileSelectButton = new Button("导入文件");
        fileSelectButton.setOnAction(this::handleFileSelect);
        batchCheckButton = new Button("批量检测");
        batchCheckButton.setStyle("-fx-base: #2196F3;");
        batchCheckButton.setOnAction(this::handleBatchCheck);
        batchCommandButton = new Button("批量执行"); // 初始化
        batchCommandButton.setStyle("-fx-base: #FF5722;");
        batchCommandButton.setOnAction(this::handleBatchCommand);
        batchButtons.getChildren().addAll(fileSelectButton, batchCheckButton, batchCommandButton);
        batchButtons.setAlignment(Pos.CENTER_RIGHT);
        gridPane.add(batchButtons, 3, 5);

        // 分隔线
        Separator separator2 = new Separator();
        gridPane.add(separator2, 0, 6, 4, 1);

        // 命令执行区域
        Text commandTitle = new Text("命令执行");
        commandTitle.setFont(Font.font("Arial", 16));
        gridPane.add(commandTitle, 0, 7, 4, 1);

        // 命令输入
        Label commandLabel = new Label("命令:");
        commandLabel.setFont(Font.font("Arial", 14));
        commandInput = new TextField();
        commandInput.setPromptText("输入要执行的命令");
        gridPane.add(commandLabel, 0, 8);
        gridPane.add(commandInput, 1, 8);

        // 命令选择
        commandSelector = new ComboBox<>();
        ObservableList<String> commandOptions = FXCollections.observableArrayList(
                "whoami",
                "pwd");
        commandSelector.setItems(commandOptions);
        commandSelector.setValue("whoami");
        gridPane.add(commandSelector, 2, 8);

        // 执行按钮
        Button commandButton = new Button("命令执行");
        commandButton.setStyle("-fx-base: #FF9800;");
        commandButton.setOnAction(this::handleCommand);

        HBox commandBox = new HBox();
        commandBox.setSpacing(10);
        commandBox.getChildren().addAll(commandButton, batchCommandButton); // 调整顺序
        gridPane.add(commandBox, 3, 8);

        // 结果展示Tab
        TabPane tabPane = new TabPane();
        Tab tab1 = new Tab("检测信息");
        textAreaVulnerability = new TextArea();
        textAreaVulnerability.setEditable(false);
        tab1.setContent(textAreaVulnerability);

        Tab tab2 = new Tab("单命令执行");
        textAreaCommandExecution = new TextArea();
        textAreaCommandExecution.setEditable(false);
        tab2.setContent(textAreaCommandExecution);

        Tab tab3 = new Tab("批量命令执行");
        textAreaBatchCommandExecution = new TextArea(); // 初始化
        textAreaBatchCommandExecution.setEditable(false); // 设置为不可编辑
        tab3.setContent(textAreaBatchCommandExecution);

        tabPane.getTabs().addAll(tab1, tab2, tab3);
        tabPane.setPrefWidth(750);
        tabPane.setPrefHeight(400);

        // 主布局
        VBox.setMargin(gridPane, new Insets(0, 0, 20, 0));
        root.getChildren().addAll(gridPane, tabPane);

        // 窗口设置
        scene.getRoot().setStyle("-fx-font-size: 14px;");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.jpg")));
        primaryStage.setTitle("ThinkPHP漏洞检测工具");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleFileSelect(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择URL列表文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("文本文件", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                List<String> lines = Files.readAllLines(selectedFile.toPath());
                batchUrls.clear();
                lines.forEach(line -> batchUrls.appendText(line + "\n"));
            } catch (Exception e) {
                textAreaVulnerability.setText("文件读取失败: " + e.getMessage());
            }
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void handleSingleCheck(ActionEvent event) {
        String targetUrl = url.getText().trim();
        if (targetUrl.isEmpty()) {
            textAreaVulnerability.setText("错误: URL不能为空");
            return;
        }

        if (!isValidUrl(targetUrl)) {
            textAreaVulnerability.setText("错误: 无效的URL格式");
            return;
        }

        textAreaVulnerability.appendText("开始检测 " + targetUrl + "...\n"); // 开始检测提示

        try {
            String selectedPoc = pocSelector.getValue();
            String payload = getPayloadForPoc(selectedPoc, "vulnerability");
            String result = vulnerabilityChecker.checkVulnerability(targetUrl, payload);

            if (result.contains("PHP Version")) {
                textAreaVulnerability.appendText(targetUrl + " [+] 漏洞存在\n");
            } else {
                textAreaVulnerability.appendText(targetUrl + " [-] 漏洞不存在\n");
            }
        } catch (Exception e) {
            textAreaVulnerability.appendText(targetUrl + " [x] 检测失败: " + e.getMessage() + "\n");
        } finally {
            textAreaVulnerability.appendText("检测 " + targetUrl + " 完成\n"); // 检测完成提示
        }
    }


    private void handleBatchCheck(ActionEvent event) {
        String[] urls = batchUrls.getText().split("\n");
        if (urls.length == 0) {
            textAreaVulnerability.setText("错误: 请输入批量URL");
            return;
        }

        textAreaVulnerability.clear();
        batchCheckButton.setDisable(true);
        textAreaVulnerability.appendText("开始批量检测...\n");

        String selectedPoc = pocSelector.getValue();
        String payload = getPayloadForPoc(selectedPoc, "vulnerability");
        int validUrlCount = 0;
        for (String targetUrl : urls) {
            if (!targetUrl.trim().isEmpty() && isValidUrl(targetUrl.trim())) {
                validUrlCount++;
            }
        }

        final int totalUrls = validUrlCount;
        AtomicInteger completedUrls = new AtomicInteger(0);

        if (totalUrls == 0) {
            textAreaVulnerability.appendText("错误: 没有有效的URL\n");
            batchCheckButton.setDisable(false);
            return;
        }

        for (String targetUrl : urls) {
            String trimmedUrl = targetUrl.trim();
            if (trimmedUrl.isEmpty() || !isValidUrl(trimmedUrl)) {
                if (!trimmedUrl.isEmpty()) {
                    textAreaVulnerability.appendText(trimmedUrl + " [x] 无效的URL格式\n");
                }
                continue;
            }

            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return vulnerabilityChecker.checkVulnerability(trimmedUrl, payload);
                }

                @Override
                protected void succeeded() {
                    int currentCount = completedUrls.incrementAndGet();
                    String result = getValue();
                    String status = result.contains("PHP Version") ? "[+] 漏洞存在" : "[-] 漏洞不存在";
                    textAreaVulnerability.appendText(trimmedUrl + " " + status + "\n");

                    if (currentCount >= totalUrls) {
                        textAreaVulnerability.appendText("\n批量检测完成\n");
                        batchCheckButton.setDisable(false);
                    }
                }

                @Override
                protected void failed() {
                    int currentCount = completedUrls.incrementAndGet();
                    textAreaVulnerability.appendText(trimmedUrl + " [x] 检测失败: " + getException().getMessage() + "\n");

                    if (currentCount >= totalUrls) {
                        textAreaVulnerability.appendText("\n批量检测完成\n");
                        batchCheckButton.setDisable(false);
                    }
                }
            };

            executorService.submit(task);
        }
    }

    private void handleBatchCommand(ActionEvent event) {
        String[] urls = batchUrls.getText().split("\n");
        if (urls.length == 0) {
            textAreaBatchCommandExecution.setText("错误: 请输入批量URL");
            return;
        }

        String command = commandInput.getText().trim();
        if (command.isEmpty()) {
            command = commandSelector.getValue();
        }

        if (command == null || command.isEmpty()) {
            textAreaBatchCommandExecution.setText("命令未选择或输入，请选择或输入一个命令！！！");
            return;
        }

        textAreaBatchCommandExecution.clear();
        batchCommandButton.setDisable(true);
        textAreaBatchCommandExecution.appendText("开始批量命令执行...\n");

        String selectedPoc = pocSelector.getValue();
        String payload = getPayloadForPoc(selectedPoc, "command", command);
        int validUrlCount = 0;
        for (String targetUrl : urls) {
            if (!targetUrl.trim().isEmpty() && isValidUrl(targetUrl.trim())) {
                validUrlCount++;
            }
        }

        final int totalUrls = validUrlCount;
        AtomicInteger completedUrls = new AtomicInteger(0);

        if (totalUrls == 0) {
            textAreaBatchCommandExecution.appendText("错误: 没有有效的URL\n");
            batchCommandButton.setDisable(false);
            return;
        }

        for (String targetUrl : urls) {
            String trimmedUrl = targetUrl.trim();
            if (trimmedUrl.isEmpty() || !isValidUrl(trimmedUrl)) {
                if (!trimmedUrl.isEmpty()) {
                    textAreaBatchCommandExecution.appendText(trimmedUrl + " [x] 无效的URL格式\n");
                }
                continue;
            }

            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return vulnerabilityChecker.executeCommand(trimmedUrl, payload);
                }

                @Override
                protected void succeeded() {
                    int currentCount = completedUrls.incrementAndGet();
                    String result = getValue();
                    textAreaBatchCommandExecution.appendText(trimmedUrl + " 执行结果:\n" + result + "\n\n");

                    if (currentCount >= totalUrls) {
                        textAreaBatchCommandExecution.appendText("\n批量命令执行完成\n");
                        batchCommandButton.setDisable(false);
                    }
                }

                @Override
                protected void failed() {
                    int currentCount = completedUrls.incrementAndGet();
                    textAreaBatchCommandExecution
                            .appendText(trimmedUrl + " [x] 命令执行失败: " + getException().getMessage() + "\n");

                    if (currentCount >= totalUrls) {
                        textAreaBatchCommandExecution.appendText("\n批量命令执行完成\n");
                        batchCommandButton.setDisable(false);
                    }
                }
            };

            executorService.submit(task);
        }
    }

    private void handleCommand(ActionEvent event) {
        String command = commandInput.getText().trim();
        if (command.isEmpty()) {
            command = commandSelector.getValue();
        }

        if (command == null || command.isEmpty()) {
            textAreaCommandExecution.setText("命令未选择或输入，请选择或输入一个命令！！！");
            return;
        }

        String targetUrl = url.getText().trim();
        if (targetUrl.isEmpty()) {
            textAreaCommandExecution.setText("错误: URL不能为空");
            return;
        }

        if (!isValidUrl(targetUrl)) {
            textAreaCommandExecution.setText("错误: 无效的URL格式");
            return;
        }

        textAreaCommandExecution.appendText("开始执行命令 " + command + " 在 " + targetUrl + "...\n"); // 开始命令执行提示

        try {
            String selectedPoc = pocSelector.getValue();
            String payload = getPayloadForPoc(selectedPoc, "command", command);
            String result = vulnerabilityChecker.executeCommand(targetUrl, payload);
            textAreaCommandExecution.appendText("命令执行结果:\n" + result + "\n");
        } catch (Exception e) {
            textAreaCommandExecution.appendText("命令执行失败: " + e.getMessage() + "\n");
        } finally {
            textAreaCommandExecution.appendText("命令执行 " + command + " 在 " + targetUrl + " 完成\n"); // 命令执行完成提示
        }
    }

    private String getPayloadForPoc(String poc, String type) {
        return HttpUtils.generatePayload(poc, type, null);
    }

    private String getPayloadForPoc(String poc, String type, String command) {
        return HttpUtils.generatePayload(poc, type, command);
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
