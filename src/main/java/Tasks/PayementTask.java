package Tasks;

import controller.Mojoodi;
import exceptions.NotFoundObjException;
import org.apache.log4j.Logger;
import view.MojoodiVO;
import view.PardakhtVO;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class PayementTask implements Runnable {
    private Mojoodi mojoodi = new Mojoodi();
    private Logger logger = Logger.getLogger(PayementTask.class);
    ReentrantLock lock = new ReentrantLock();
    List<MojoodiVO> mojoodiVOS;
    List<PardakhtVO> pardakhtVOS;

    public PayementTask(List<MojoodiVO> mojoodiVOS, List<PardakhtVO> pardakhtVOS) {
        this.mojoodiVOS = mojoodiVOS;
        this.pardakhtVOS = pardakhtVOS;
    }

    @Override
    public void run() {
        payment(mojoodiVOS, pardakhtVOS);
    }

    public void payment(List<MojoodiVO> mojoodiVOS, List<PardakhtVO> pardakhtVOS) {
        lock.lock();

        try {
            boolean isEnoughMoney = false;
            try {
                isEnoughMoney = mojoodi.checkTheMount(mojoodiVOS, pardakhtVOS);
            } catch (NotFoundObjException e) {
                logger.error(e.getMessage());
            }

            if (isEnoughMoney) {
                logger.info("you can pay by this deposite number: " + Mojoodi.debtorDeposit);
                String updatedMojoodiText = mojoodi.updateDebtorMount(mojoodiVOS, pardakhtVOS);
                String updatedCreditors = mojoodi.updateCreditorMount(mojoodiVOS, pardakhtVOS);
                updatedMojoodiText = updatedMojoodiText.concat(updatedCreditors);
                mojoodi.writeUpdatedMojoodiText(updatedMojoodiText);
                logger.info("the paying salary is done");

            } else {
                logger.warn("you dont have enough money ! please check your bank account.");
            }
        } finally {
            lock.unlock();
        }

    }
}
