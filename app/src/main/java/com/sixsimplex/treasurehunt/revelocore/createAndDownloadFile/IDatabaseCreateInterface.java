package com.sixsimplex.treasurehunt.revelocore.createAndDownloadFile;

public interface IDatabaseCreateInterface {

    void successFileDownload(int requestCode, String message);
    void errorFileDownload(int requestCode, int errorCode,String message);
}
