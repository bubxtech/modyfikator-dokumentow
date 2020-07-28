package pl.pentacomp.ModyfikatorDokumentow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ModyfikatorDokumentowApplication extends Application {

	private static final String FRAME_TITLE = "Modyfikator dokumentów v.1.0.0 - Zespół NT-4 - Jakub Walczak";

	private ConfigurableApplicationContext springContext;
	private Parent rootNode;
	private FXMLLoader fxmlLoader;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {
		springContext = new SpringApplicationBuilder(ModyfikatorDokumentowApplication.class)
								.headless(false).run();

		fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
	}

	@Override
	public void stop() throws Exception {
		springContext.stop();
		System.exit(0);
	}

	@Override
	public void start(Stage primaryStage) throws Exception{
		fxmlLoader.setLocation(getClass().getResource("/fxml/mainFrame.fxml"));
		rootNode = fxmlLoader.load();

		primaryStage.setTitle(FRAME_TITLE);
		Scene scene = new Scene(rootNode, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
