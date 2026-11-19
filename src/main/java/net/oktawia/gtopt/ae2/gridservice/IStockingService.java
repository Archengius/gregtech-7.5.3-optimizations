package net.oktawia.gtopt.ae2.gridservice;

import net.oktawia.gtopt.ae2.IStockingPartOpt;

import appeng.api.networking.IGridService;
import appeng.api.stacks.AEKey;

public interface IStockingService extends IGridService {

    void markForRefresh(IStockingPartOpt part);

    void markForAutoPull(IStockingPartOpt part);

    void markStockDirty(IStockingPartOpt part, AEKey key);
}
