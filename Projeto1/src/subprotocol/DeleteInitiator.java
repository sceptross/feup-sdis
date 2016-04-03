package subprotocol;

import communication.message.DeleteMessage;
import general.FilesMetadataManager;
import general.MulticastChannel;

import java.io.IOException;

/**
 * Created by Afonso on 02/04/2016.
 */
public class DeleteInitiator {
    private static final int ATTEMPTS = 5;

    private String fileId;
    private String localId;
    private String filePath;
    private MulticastChannel mcChannel;

    public DeleteInitiator(String filename, String localId, MulticastChannel mcChannel){
        fileId = FilesMetadataManager.getInstance().getFileId(filename);
        this.localId = localId;
        this.filePath = filename;
        this.mcChannel = mcChannel;
    }

    public void deleteFile() throws IOException {
        for(int attempt = 0; attempt < ATTEMPTS; ++attempt) {
            byte[] message = new DeleteMessage(localId, fileId).getBytes();
            mcChannel.send(message);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) { }

        }
    }

    public void setMetadata() throws IOException {
        FilesMetadataManager.getInstance().removeIfExists(filePath);
    }
}
