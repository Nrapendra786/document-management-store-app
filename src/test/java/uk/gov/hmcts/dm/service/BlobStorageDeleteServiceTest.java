package uk.gov.hmcts.dm.service;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class BlobStorageDeleteServiceTest {

    private BlobStorageDeleteService blobStorageDeleteService;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private BlockBlobClient blob;

    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    final StoredDocument storedDocument = createStoredDocument();
    final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);

    private Response mockResponse = mock(Response.class);


    @Before
    public void setUp() {
        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        blobStorageDeleteService = new BlobStorageDeleteService(cloudBlobContainer, documentContentVersionRepository);
    }

    @Test
    public void delete_documentContentVersion() {
        when(mockResponse.getStatusCode()).thenReturn(202);
        given(blob.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .willReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(documentContentVersionRepository, times(1))
            .updateContentUriAndContentCheckSum(documentContentVersion.getId(), null, null);
    }

    @Test
    public void delete_documentContentVersion_if_responseCode_404() {
        when(mockResponse.getStatusCode()).thenReturn(404);
        when(blob.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(documentContentVersionRepository, times(1))
            .updateContentUriAndContentCheckSum(documentContentVersion.getId(), null, null);
    }

    @Test
    public void not_delete_documentContentVersion_if_responseCode_not_202_or_404() {
        when(mockResponse.getStatusCode()).thenReturn(409);
        when(blob.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(documentContentVersionRepository, never())
            .updateContentUriAndContentCheckSum(any(), any(), any());
    }

    @Test
    public void not_delete_DocumentContentVersion_if_blob_delete_fails_with_exception() {
        var blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getStatusCode()).thenReturn(409);
        when(blob.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(blobStorageException);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(documentContentVersionRepository, never()).updateContentUriAndContentCheckSum(any(), any(), any());
    }

    @Test
    public void delete_documentContentVersion_if_blob_does_not_exist() {
        var blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getStatusCode()).thenReturn(404);
        when(blob.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(blobStorageException);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(documentContentVersionRepository, times(1))
            .updateContentUriAndContentCheckSum(documentContentVersion.getId(), null, null);
    }

    private StoredDocument createStoredDocument() {
        return createStoredDocument(randomUUID());
    }

    private StoredDocument createStoredDocument(UUID documentContentVersionUuid) {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(randomUUID());
        storedDocument.setDocumentContentVersions(singletonList(buildDocumentContentVersion(documentContentVersionUuid,
            storedDocument)));
        return storedDocument;
    }

    private DocumentContentVersion buildDocumentContentVersion(
        UUID documentContentVersionUuid,
        StoredDocument storedDocument
    ) {
        DocumentContentVersion doc = new DocumentContentVersion();
        doc.setId(documentContentVersionUuid);
        doc.setStoredDocument(storedDocument);
        doc.setSize(1L);
        return doc;
    }
}
