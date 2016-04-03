package general;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Afonso on 31/03/2016.
 */
public class ChunksMetadataManager {

    private static final String CHUNKS_METADATA_FILENAME = "chunks.metadata";

    private class Entry {
        String fileId;
        String chunkNo;
        String repDegree;
        Set<String> peers;

        Entry(String fileId, String chunkNo, String repDegree, Set<String> peers) {
            this.fileId = fileId;
            this.chunkNo = chunkNo;
            this.repDegree = repDegree;
            this.peers = peers;
        }

        public String toString(){
            StringJoiner sb = new StringJoiner("|");
            sb.add(fileId)
                .add(chunkNo)
                .add(repDegree);
            for (String peer : peers)
                sb.add(peer);

            return sb.toString();
        }
    }

    private List<Entry> metadata;
    private File file;
    private static ChunksMetadataManager instance = null;

    private ChunksMetadataManager(){
        metadata = new ArrayList<>();
    }

    public static ChunksMetadataManager getInstance(){
        if (instance == null)
            instance = new ChunksMetadataManager();
        return instance;
    }

    public void init(String localId) throws IOException {
        Path path = FileSystems.getDefault().getPath("peer" + localId, CHUNKS_METADATA_FILENAME);
        file = path.toFile();

        if (!file.exists()) {
            file.createNewFile();
        }

        load();
    }

    private void save() throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for(Entry m : metadata){
            bw.write(m.toString());
            bw.newLine();
        }
        bw.close();
    }

    private void load() throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        try {
            String line;
            while((line = br.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Entry findChunk(String fileId, String chunkNo){
        for(Entry f : metadata){
            if (Objects.equals(f.fileId, fileId) && Objects.equals(f.chunkNo, chunkNo))
                return f;
        }
        return null;
    }

    private void parseLine(String line){
        String[] elements = line.split("\\|");
        String fileId = elements[0];
        String chunkNo = elements[1];
        String repDegree = elements[2];
        Set<String> senders = new HashSet<>();
        senders.addAll(Arrays.asList(elements).subList(3, elements.length));
        metadata.add(new Entry(fileId, chunkNo, repDegree, senders));
    }

    public void removeFileIfExists(String fileId, String chunkNo) throws IOException {
        Entry f = findChunk(fileId, chunkNo);
        if (f != null){
            metadata.remove(f);
            save();
        }
    }

    public List<String> getChunksFromFile(String fileId) {
        List<String> ret = new ArrayList<>();
        for(Entry m : metadata)
            if (Objects.equals(m.fileId, fileId))
                ret.add(m.chunkNo);
        return ret;
    }

    public void removePeerIfExists(String fileId, String chunkNo, String peer) throws IOException {
        Entry f = findChunk(fileId, chunkNo);
        if (f != null){
            if(f.peers.contains(peer)) {
                f.peers.remove(peer);
                save();
            }
        }
    }

    public void addPeerIfNotExists(String fileId, String chunkNo, String peer) throws IOException {
        Entry f = findChunk(fileId, chunkNo);
        if (f != null){
            if(!f.peers.contains(peer)) {
                f.peers.add(peer);
                save();
            }
        }
    }

    public void addFileIfNotExists(String fileId, String chunkNo, String replicationDeg, Set<String> peers) throws IOException {
        Entry f = findChunk(fileId, chunkNo);
        if (f == null){
            metadata.add(new Entry(fileId, chunkNo, replicationDeg, peers));
            save();
        }
    }

    public Set<ChunkIdentifier> getBackedUpChunks(){
        Set<ChunkIdentifier> ret = new HashSet<>();
        for(Entry e : metadata){
            ret.add(new ChunkIdentifier(e.fileId, e.chunkNo));
        }
        return ret;
    }
}