package chenwei.tool_set.download;

public class DownloadConfiguer {
  int threadNum;
  String defaultDir;
  int blockSize;

  public DownloadConfiguer() {
    super();
  }

  public DownloadConfiguer(int threadNum, String defaultDir) {
    super();
    this.threadNum = threadNum;
    this.defaultDir = defaultDir;
  }

  public int getThreadNum() {
    return threadNum;
  }

  public void setThreadNum(int threadNum) {
    this.threadNum = threadNum;
  }

  public String getDefaultDir() {
    return defaultDir;
  }

  public void setDefaultDir(String defaultDir) {
    this.defaultDir = defaultDir;
  }

  public int getBlockSize() {
    return blockSize;
  }

  public void setBlockSize(int blockSize) {
    this.blockSize = blockSize;
  }

}
