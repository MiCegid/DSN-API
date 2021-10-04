package fr.gipmds.dsn.test.depot;

import com.github.mjeanroy.junit.servers.jupiter.JunitServerExtension;
import fr.gipmds.dsn.test.BaseTestClass;
import fr.gipmds.dsn.test.resources.TestData;
import fr.gipmds.dsn.utils.Base64Utils;
import fr.gipmds.dsn.utils.SecurityUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(JunitServerExtension.class)
@DisplayName("Deposer Dsn MSA Test")
class DeposerDsnMSATest extends BaseTestClass {

    @Test
    @DisplayName("Deposer 415 Content Encoding Manquant Test")
    void deposer415ContentEncodingManquantTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Entity<byte[]> entity = Entity.entity(data, MediaType.TEXT_PLAIN); //  content encoding manquant
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(415, response.getStatus());
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }

    @Test
    @DisplayName("Deposer 415 Content Encoding Mauvais Test")
    void deposer415ContentEncodingMauvaisTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "deflate"); // doit contenir gzip
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(415, response.getStatus());
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }

    @Test
    @DisplayName("Deposer 400 No Body Test")
    void deposer400NoBodyTest() throws Exception {
        // Impossible de tester ce cas avec RestEasy, car le framework ne
        // permet pas de construire un body null (renvoie une NPE)
        // Given
        URL url = new URL(getServerUrl() + "/deposer-dsn/1.0/");
        // When
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Encoding", "gzip");
        connection.setRequestProperty("Content-Type", "text/plain");
        // Pas de body

        // Then
        // Standard HTTP error due to "EOFException"
        assertEquals(400, connection.getResponseCode());
        assertEquals("text/html", connection.getHeaderField("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 406 Accept Encoding Mauvais Test")
    void deposer406AcceptEncodingMauvaisTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder
                .acceptEncoding("deflate")  // doit contenir gzip
                .buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(406, response.getStatus());
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }

    @Test
    @DisplayName("Deposer 415 Content Type Mauvais Test")
    void deposer415ContentTypeMauvaisTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.APPLICATION_XML), Locale.FRANCE,
                "gzip"); // MediaType TEXT PLAIN à utiliser
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(415, response.getStatus());
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }

    @Test
    @DisplayName("Deposer 200 Test")
    void deposer200Test() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        // builder.header("Accept", ContentType.APPLICATION_XML.getMimeType());
        Invocation invocation = builder.buildPost(entity);
        // When
        Response actual = invocation.invoke();
        // Then
        assertEquals(200, actual.getStatus());
        assertEquals("gzip", actual.getHeaderString("Content-Encoding"));
        assertEquals("application/xml", actual.getHeaderString("Content-Type"));
        assertNull(actual.getHeaderString("Transfer-Encoding"));
        byte[] bytes = new byte[]{};
        byte[] actualBody = actual.readEntity(bytes.getClass());
        assertEquals(TestData.aee, new String(actualBody));
    }

    @Test
    @Disabled(value = "PoC seulement : dépend du serveur et de sa configuration")
    @DisplayName("Deposer 200 AEE Chunked Test")
    void deposer200AEEChunkedTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response actual = invocation.invoke();
        // Then
        assertEquals(200, actual.getStatus());
        assertEquals("gzip", actual.getHeaderString("Content-Encoding"));
        assertEquals("application/xml", actual.getHeaderString("Content-Type"));
        assertEquals("chunked", actual.getHeaderString("Transfer-Encoding"));
        byte[] bytes = new byte[]{};
        byte[] actualBody = actual.readEntity(bytes.getClass());
        assertEquals(TestData.aee.getBytes(), actualBody, "UTF-8");
    }

    @Test
    @DisplayName("Deposer 200 Accept Encoding GZIP Test")
    void deposer200AcceptEncodingGZIPTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        Builder builder = client.target(getServerUrl() + "/deposer-dsn/1.0/").request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.acceptEncoding("gzip").buildPost(entity);
        // When
        Response actual = invocation.invoke();
        // Then
        assertEquals(200, actual.getStatus());
        assertEquals("gzip", actual.getHeaderString("Content-Encoding"));
        assertEquals("application/xml", actual.getHeaderString("Content-Type"));
        // L'API utilisé ne permettent pas d'envoyé la requette avec l'entete chunked.
        // mais le principe est le même.
        // assertEquals("chunked", actual.getHeaderString("Transfer-Encoding"));
        byte[] bytes = new byte[]{};
        byte[] actualBody = actual.readEntity(bytes.getClass());
        assertEquals(TestData.aee, new String(actualBody));
    }

    @Test
    @DisplayName("Deposer 422 Avis De Rejet Test")
    void deposer422AvisDeRejetTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S11 VGhpcyBpcyBhbiBBRUU".getBytes();  // pas de bloc S10
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response actual = invocation.invoke();
        // Then
        assertEquals(422, actual.getStatus());
        assertEquals("gzip", actual.getHeaderString("Content-Encoding"));
        assertEquals("application/xml", actual.getHeaderString("Content-Type"));
        byte[] bytes = new byte[]{};
        byte[] actualBody = actual.readEntity(bytes.getClass());
        assertEquals(TestData.are, new String(actualBody));
    }

    @Test
    @DisplayName("Deposer 401 AUTHORIZATION Mauvais Test")
    void deposer401AUTHORIZATIONMauvaisTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("declarent", "SNP"); // mauvais declarant
        builder.header("Authorization", SecurityUtils.buildAuthorizationHeader(map));
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }

    @Test
    @DisplayName("Deposer 401 AUTHORIZATION Manquant Test")
    void deposer401AUTHORIZATIONManquantTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        // Authorization manquant
        Entity<byte[]> entity = Entity.entity(data, variant);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }

    @Test
    @DisplayName("Deposer 401 JETON Mauvais Test")
    void deposer401JETONMauvaisTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", Base64Utils.encode("Not a valid token")); // jeton non valide
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }

    @Test
    @DisplayName("Deposer 401 CONCENTRATEUR Mauvais Test")
    void deposer401CONCENTRATEURMauvaisTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("concentrateur", Base64Utils.encode("Not a valid token")); // valeur  concentrateur non valide
        map.put("declarant", Base64Utils.encode(TestData.declarantInscrit.getSNP()));
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 DECLARANT Mauvais Test")
    void deposer401DECLARANTMauvaisTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("concentrateur", TestData.concentrateurInscrit.getFauxJeton());
        map.put("declarant", Base64Utils.encode("Not a é valid token")); // valeur  declarant non valide
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    void deposer401JETONNullTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", null);   // valeur jeton non valide
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 CONCENTRATEUR Null Test")
    void deposer401CONCENTRATEURNullTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("concentrateur", null); // valeur concentrateur non valide
        map.put("declarant", Base64Utils.encode(TestData.declarantInscrit.getSNP()));
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 DECLARANT Null Test")
    void deposer401DECLARANTNullTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("concentrateur", TestData.concentrateurInscrit.getFauxJeton());
        map.put("declarant", null);   // valeur declarant non valide
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 JETON Non Inscrit Test")
    void deposer401JETONNonInscritTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantNonInscrit.getFauxJeton());  // valeur jeton non valide (non inscrit)
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Utilisateur non-inscrit au service\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 DECLARANT Non Inscrit Test")
    void deposer401DECLARANTNonInscritTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("concentrateur", TestData.concentrateurInscrit.getFauxJeton());
        map.put("declarant", Base64Utils.encode(TestData.declarantNonInscrit.getSNP())); // valeur declarant non valide (non inscrit)
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Utilisateur non-inscrit au service\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 CONCENTRATEUR Non Inscrit Test")
    void deposer401CONCENTRATEURNonInscritTest() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("concentrateur", TestData.concentrateurNonInscrit.getFauxJeton()); // valeur concentrateur non valide  (non inscrit)
        map.put("declarant", Base64Utils.encode(TestData.declarantInscrit.getSNP()));
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Utilisateur non-inscrit au service\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 JETON Pas En Base 64 Test")
    void deposer401JETONPasEnBase64Test() throws Exception {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", URLEncoder.encode("Not a base 64 token", "UTF-8")); // valeur jeton non valide (pas en base 64)
        builder.header("Authorization", SecurityUtils.buildAuthorizationHeader(map));
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 CONCENTRATEUR Pas En Base 64 Test")
    void deposer401CONCENTRATEURPasEnBase64Test() throws Exception {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("concentrateur", URLEncoder.encode("Not a base 64 token", "UTF-8")); // valeur concentrateur non valide (pas en base 64)
        map.put("declarant", Base64Utils.encode(TestData.declarantInscrit.getSNP()));
        builder.header("Authorization", SecurityUtils.buildAuthorizationHeader(map));
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
    }

    @Test
    @DisplayName("Deposer 401 DECLARANT Pas En Base 64 Test")
    void deposer401DECLARANTPasEnBase64Test() {
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 VGhpcyBpcyBhbiBBRUU".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("concentrateur", TestData.concentrateurInscrit.getFauxJeton());
        map.put("declarant", TestData.declarantInscrit.getSNP()); // valeur declarant non valide (pas en base 64)
        builder.header("Authorization", SecurityUtils.buildAuthorizationHeader(map));
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(401, response.getStatus());
        assertEquals("DSNLogin realm=\"Jeton manquant ou invalide\"", response.getHeaderString("WWW-Authenticate"));
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }

    @Test
    @DisplayName("Deposer 500 Test")
    void deposer500Test() {
        // ce test ne contient pas d'erreur côté client, c'est une erreur côté serveur
        // Given
        ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
        ResteasyWebTarget target = client.target(getServerUrl() + "/deposer-dsn/1.0/");
        Builder builder = target.request();
        // Entity
        byte[] data = "S10 KABOOM".getBytes();
        Variant variant = new Variant(MediaType.valueOf(MediaType.TEXT_PLAIN), Locale.FRANCE, "gzip");
        Entity<byte[]> entity = Entity.entity(data, variant);
        // Authorization
        Map<String, String> map = new HashMap<>();
        map.put("jeton", TestData.declarantInscrit.getFauxJeton());
        String authorization = SecurityUtils.buildAuthorizationHeader(map);
        builder.header("Authorization", authorization);
        Invocation invocation = builder.buildPost(entity);
        // When
        Response response = invocation.invoke();
        // Then
        assertEquals(500, response.getStatus());
        assertNull(response.getHeaderString("Content-Type"));
        assertEquals(0, response.getLength());
    }
}
