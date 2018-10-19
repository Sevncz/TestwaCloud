package com.testwa.distest.server.web.wallet.controller;


import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.vo.ResultVO;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.web.wallet.mgr.WalletMgr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api("钱包操作相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/wallet")
public class WalletController extends BaseController {

    @Autowired
    private WalletMgr walletMgr;

    @ApiOperation(value="个人使用设备时长总计")
    @ResponseBody
    @GetMapping(value = "/equipment/duration/total")
    public ResultVO totalEquipmentDuration() {
        String username = WebUtil.getCurrentUsername();

        Long sumTime = walletMgr.totalEquipmentDuration(username);

        return ok(sumTime);
    }

}
