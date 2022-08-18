package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ReadThumbnailIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void RT1__As_an_authenticated_when_i_upload_a_Tiff_I_get_an_icon_in_return() throws IOException {
        Response response = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_25_TIFF()), V1MimeTypes.IMAGE_TIF_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getATTACHMENT_25_TIFF()))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_TIF_VALUE))
            .body("_embedded.documents[0].classification", equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .andReturn();

        String tiffUrl = response.path("_embedded.documents[0]._links.thumbnail.href");

        byte[] tiffByteArray = givenRequest(getCITIZEN())
            .get(tiffUrl)
            .asByteArray();

        byte[] file = Files.readAllBytes(file("ThumbnailNPad.jpg").toPath());

        Assert.assertArrayEquals(tiffByteArray, file);
    }

    @Test
    public void RT2__As_an_authenticated_user_when_I_upload_a_bmp__I_can_get_the_thumbnail_of_that_bmp() {
        String url = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_26_BMP()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_26_BMP()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        Assert.assertNotNull(givenRequest(getCITIZEN())
            .get(url)
            .asByteArray());
    }

    @Test
    public void RT3__As_an_authenticated_user_when_I_upload_a_bmp__I_can_get_the_version_of_thumbnail_of_that_bmp() {
        String url = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_26_BMP()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_26_BMP()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href",
                Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href");

        Assert.assertNotNull(givenRequest(getCITIZEN()).get(url).asByteArray());
    }

    @Test
    public void RT4__As_an_unauthenticated_user_I_can_not_get_the_version_of_thumbnail_of_that_bmp() {
        String url = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_26_BMP()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen").expect().log().all().statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_26_BMP()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href",
                Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href");

        givenUnauthenticatedRequest()
            .when()
            .get(url)
            .then()
            .assertThat()
            .statusCode(403)
            .body("error", Matchers.equalTo("Access Denied"))
            .log().all();
    }

    @Test
    public void RT5__As_unauthenticated_user_I_can_not_get_the_thumbnail_of_a_bmp() {
        String url = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_26_BMP()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_26_BMP()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        givenUnauthenticatedRequest()
            .when()
            .get(url)
            .then()
            .assertThat()
            .statusCode(403)
            .body("error", Matchers.equalTo("Access Denied"))
            .log().all();
    }

    @Test
    public void RT6__As_an_authenticated_user__I_can_not_find_the_thumbnail_of_non_existent_bmp() {
        String url = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_26_BMP()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", equalTo(getATTACHMENT_26_BMP()))
            .body("_embedded.documents[0].mimeType", equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", equalTo( String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._links.thumbnail.href", containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._links.thumbnail.href");

        String documentStr = "documents/";

        String documentId = url.substring(url.indexOf(documentStr) + documentStr.length(), url.lastIndexOf("/"));

        String nonExistentId = UUID.randomUUID().toString();

        String nonExistentIdURL = url.replace(documentId, nonExistentId);

        givenRequest(getCITIZEN())
            .when()
            .get(nonExistentIdURL)
            .then()
            .assertThat()
            .statusCode(404)
            .log().all();
    }

    @Test
    public void RT7__As_an_authenticated_user__I_can_not_find_the_version_of_thumbnail_of_non_existent_bmp() {
        String url = givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_26_BMP()), V1MimeTypes.IMAGE_BMP_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(200)
            .contentType(V1MediaTypes.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE_VALUE)
            .body("_embedded.documents[0].originalDocumentName", Matchers.equalTo(getATTACHMENT_26_BMP()))
            .body("_embedded.documents[0].mimeType", Matchers.equalTo(V1MimeTypes.IMAGE_BMP_VALUE))
            .body("_embedded.documents[0].classification", Matchers.equalTo(String.valueOf(Classifications.PUBLIC)))
            .body("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href",
                Matchers.containsString("thumbnail"))
            .when()
            .post("/documents")
            .path("_embedded.documents[0]._embedded.allDocumentVersions._embedded.documentVersions[0]._links.thumbnail.href");

        String versionsStr = "versions/";
        String versionId = url.substring(url.indexOf(versionsStr) + versionsStr.length(), url.lastIndexOf("/"));
        String nonExistentVersionId = UUID.randomUUID().toString();
        String nonExistentVersionIdURL = url.replace(versionId, nonExistentVersionId);

        givenRequest(getCITIZEN())
            .when()
            .get(nonExistentVersionIdURL)
            .then()
            .assertThat()
            .statusCode(404)
            .log().all();
    }
}
