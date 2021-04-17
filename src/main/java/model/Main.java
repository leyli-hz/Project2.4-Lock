package model;

import Tasks.PayementTask;
import common.FileHandling;
import controller.Mojoodi;
import controller.Pardakht;
import exceptions.FilesException;
import org.apache.log4j.Logger;
import view.MojoodiVO;
import view.PardakhtVO;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class);
        Pardakht pardakht = new Pardakht();
        Mojoodi mojoodi = new Mojoodi();
//create Files
        FileHandling fileHandling = new FileHandling();
        fileHandling.createFile("transaction");
        fileHandling.createFile("mojoodi");
        fileHandling.createFile("pardakht");
//write on Files
        boolean isPardakhtEmpty = fileHandling.isEmpty(pardakht.getPath());
        if (isPardakhtEmpty) {
            String contentPardakht = fileHandling.makePardakhtContext("1.10.100.1", "2.20.200.1");
            try {
                fileHandling.writeOnFile(contentPardakht, Paths.get("Files/pardakht.txt"));
            } catch (FilesException e) {
                logger.error(e.getMessage(), e.getCause());
            }
        } else {
            logger.warn("#Pardakht file is not empty. paying salary will continue with previouse data.\r\n" +
                    "for new data Please delete file context before run the program!");
        }

        boolean isMojoodiEmpty = fileHandling.isEmpty(mojoodi.getPath());
        if (isMojoodiEmpty) {
            String contentMojoodi = fileHandling.makeMojoodiContext("1.10.100.1", "2.20.200.1");
            try {
                fileHandling.writeOnFile(contentMojoodi, Paths.get("Files/mojoodi.txt"));
            } catch (FilesException e) {
                logger.error(e.getMessage(), e.getCause());
            }
        } else {
            logger.warn("#Mojoodi file is not empty! paying salary will continue with previouse data.\r\n " +
                    "for new data Please delete file context before run the program!");
        }

        List<MojoodiVO> mojoodiVOS = mojoodi.exportToMojoodiVO();
        List<PardakhtVO> pardakhtVOS = pardakht.exportToPardakhtVO();

        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(coreCount);
        Runnable paymentTask = new PayementTask(mojoodiVOS, pardakhtVOS);
        for (int i = 0; i < 3; i++) {
            executorService.submit(paymentTask);
        }

        //IntStram and synchronized
       /* ExecutorService executorService = Executors.newFixedThreadPool(coreCount);
        Runnable paymentTask = new tasks.PayementTask(mojoodiVOS, pardakhtVOS);
        IntStream.range(0, 3)
                .forEach(i -> executorService.submit(paymentTask ::run));*/

        executorService.shutdown();
    }
}
