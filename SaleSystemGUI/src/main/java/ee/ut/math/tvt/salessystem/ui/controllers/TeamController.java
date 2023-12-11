package ee.ut.math.tvt.salessystem.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.fxml.Initializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;


@SuppressWarnings("JavaFXVersion")
public class TeamController implements Initializable {

    private static final Logger log = LogManager.getLogger(TeamController.class);

    @FXML
    private Text teamName;
    @FXML
    private Text teamContact;
    @FXML
    private Text teamMembers;
    @FXML
    private ImageView teamLogo;

    private String name;
    private String membersName;
    private String teamMail;
    private Image image;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Properties properties = new Properties();

        try (InputStream input = getClass().getResourceAsStream("/app/src/main/resources/application.properties")) {            properties.load(input);
            name = properties.getProperty("team.name");
            teamMail = properties.getProperty("team.contact");
            membersName = properties.getProperty("team.members");
            image = new Image(properties.getProperty("team.logo.path"));

            teamName.setText(name);
            teamContact.setText(teamMail);
            teamMembers.setText(membersName);
            teamLogo.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error loading team information", e);
        }
        log.debug("TeamController initialization complete.");

    }
}
