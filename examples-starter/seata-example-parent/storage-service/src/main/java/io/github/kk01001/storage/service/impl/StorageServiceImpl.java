package io.github.kk01001.storage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.kk01001.storage.entity.Storage;
import io.github.kk01001.storage.mapper.StorageMapper;
import io.github.kk01001.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final StorageMapper storageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(String commodityCode, int count) {
        log.info("Storage Service Begin ... xid: {}", io.seata.core.context.RootContext.getXID());
        Storage storage = storageMapper.selectOne(new LambdaQueryWrapper<Storage>()
                .eq(Storage::getCommodityCode, commodityCode));
        if (storage == null) {
            throw new RuntimeException("商品不存在");
        }
        if (storage.getCount() < count) {
            throw new RuntimeException("库存不足");
        }
        storage.setCount(storage.getCount() - count);
        storageMapper.updateById(storage);
        log.info("Storage Service End ... ");
    }
}
