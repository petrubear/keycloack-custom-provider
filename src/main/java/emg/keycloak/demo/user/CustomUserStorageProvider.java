package emg.keycloak.demo.user;

import emg.keycloak.demo.db.DBUtil;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;

public class CustomUserStorageProvider implements UserStorageProvider,
    UserLookupProvider,
    CredentialInputValidator,
    UserQueryProvider {

    private KeycloakSession ksession;
    private ComponentModel model;

    public CustomUserStorageProvider(KeycloakSession ksession, ComponentModel model) {
        this.ksession = ksession;
        this.model = model;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.endsWith(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!this.supportsCredentialType(credentialInput.getType())) {
            return false;
        }
        StorageId sid = new StorageId(user.getId());
        String username = sid.getExternalId();

        try (Connection c = DBUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select password from TSEC_USER where USERNAME = ?");
            st.setString(1, username);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                String pwd = rs.getString(1);
                if (pwd != null) {
                    return true;
                }
            } else {
                return false;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        return getUserByUsername(realm, storageId.getExternalId());
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        try (Connection c = DBUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select NAMES, LAST_NAME, PERSONAL_EMAIL from tsec_user where username = ?");
            st.setString(1, username);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return mapUser(realm, username, rs);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        try (Connection c = DBUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select NAMES, LAST_NAME, USERNAME from tsec_user where PERSONAL_EMAIL = ?");
            st.setString(1, email);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return mapUserByEmail(realm, email, rs);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        return null;
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        return null;
    }


    private UserModel mapUser(RealmModel realm, String username, ResultSet rs) throws SQLException {
        CustomUser user = new CustomUser.Builder(ksession, realm, model, username)
            .email(rs.getString("PERSONAL_EMAIL"))
            .firstName(rs.getString("NAMES"))
            .lastName(rs.getString("LAST_NAME"))
            .build();

        return user;
    }

    private UserModel mapUserByEmail(RealmModel realm, String email, ResultSet rs) throws SQLException {
        CustomUser user = new CustomUser.Builder(ksession, realm, model, rs.getString("USERNAME"))
            .email(email)
            .firstName(rs.getString("NAMES"))
            .lastName(rs.getString("LAST_NAME"))
            .build();

        return user;
    }
}
