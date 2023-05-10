package emg.keycloak.demo.user;

import emg.keycloak.demo.db.DBUtil;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.sql.Connection;
import java.util.List;

import static emg.keycloak.demo.constants.Constants.*;

public class CustomUserStorageProviderFactory implements UserStorageProviderFactory<CustomUserStorageProvider> {
    protected final List<ProviderConfigProperty> configMetadata;

    public CustomUserStorageProviderFactory() {
        this.configMetadata = ProviderConfigurationBuilder.create()
            .property()
            .name(CONFIG_KEY_JDBC_DRIVER)
            .label("JDBC Driver Class")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("oracle.jdbc.OracleDriver")
            .helpText("Fully qualified class name of the JDBC driver")
            .add()
            .property()
            .name(CONFIG_KEY_JDBC_URL)
            .label("JDBC URL")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("jdbc:oracle:thin:@192.168.5.131:1521:modinter")
            .helpText("JDBC URL used to connect to the user database")
            .add()
            .property()
            .name(CONFIG_KEY_DB_USERNAME)
            .label("Database User")
            .type(ProviderConfigProperty.STRING_TYPE)
            .helpText("Username used to connect to the database")
            .add()
            .property()
            .name(CONFIG_KEY_DB_PASSWORD)
            .label("Database Password")
            .type(ProviderConfigProperty.STRING_TYPE)
            .helpText("Password used to connect to the database")
            .secret(true)
            .add()
            .property()
            .name(CONFIG_KEY_VALIDATION_QUERY)
            .label("SQL Validation Query")
            .type(ProviderConfigProperty.STRING_TYPE)
            .helpText("SQL query used to validate a connection")
            .defaultValue("select 1 from dual")
            .add()
            .build();
    }

    @Override
    public CustomUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new CustomUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return "EMG-custom-user-storage-provider";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        try (Connection c = DBUtil.getConnection(config)) {
            c.createStatement().execute(config.get(CONFIG_KEY_VALIDATION_QUERY));
        } catch (Exception ex) {
            throw new ComponentValidationException("Unable to validate database connection", ex);
        }
    }
}
