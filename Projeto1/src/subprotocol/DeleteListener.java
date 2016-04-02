package subprotocol;

import communication.Message;
import communication.message.DeleteMessage;
import general.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by afonso on 26-03-2016.
 */
public class DeleteListener extends SubProtocolListener implements Observer {
    private static final DeleteMessage.Parser parser = new DeleteMessage.Parser();

    public DeleteListener(String localId, MulticastChannelManager mcm) {
        super(localId, mcm);
    }

    @Override
    public void update(Observable o, Object arg) {
        Message msg = null;
        try {
            msg = parser.parse((byte[]) arg);
            String filePrefix = "peer" + getLocalId() + "/" + msg.getFileId();
            List<String> chunks = ChunksMetadataManager.getInstance().getChunksFromFile(msg.getFileId());
            for (String chunk : chunks) {
                String fileName = filePrefix + "-" + chunk + ".chunk";
                File f = new File(fileName);
                ChunksMetadataManager.getInstance().removeFileIfExists(msg.getFileId(), chunk);
                if (f.exists() && !f.isDirectory()) {
                    f.delete();
                }
            }
            Logger.getInstance().printLog(msg.getHeader());
        } catch (IOException | MalformedMessageException e) {
            e.printStackTrace();
        }
    }
}
