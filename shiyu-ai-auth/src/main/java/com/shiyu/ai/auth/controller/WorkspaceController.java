package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.service.WorkspaceService;
import com.shiyu.ai.auth.bo.WorkspaceBO;
import com.shiyu.ai.auth.request.WorkspaceRequest;
import com.shiyu.ai.auth.vo.WorkspaceVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宸ヤ綔绌洪棿绠＄悊 Controller
 */
@Slf4j
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 鑾峰彇宸ヤ綔绌洪棿鍒楄〃锛堟爲褰級锛屽钩閾鸿浆鎹㈤伩鍏嶅惊鐜紩鐢ㄥ鑷寸殑 StackOverflowError
     */
    @GetMapping("/list")
    public Result<List<WorkspaceVO>> getWorkspaceList(
            @RequestParam(required = false) String name) {
        log.info("鑾峰彇宸ヤ綔绌洪棿鍒楄〃锛宯ame: {}", name);

        List<WorkspaceBO> workspaceBOs = workspaceService.getWorkspaceList(name);

        // 鍏堝钩閾烘爲 -> 娓呯┖ children 閬垮厤閫掑綊杞崲瀵艰嚧寰幆寮曠敤
        List<WorkspaceBO> flatBos = flattenBos(workspaceBOs);
        List<WorkspaceVO> flatVos = MapstructUtils.convert(flatBos, WorkspaceVO.class);

        // 浠庢墎骞?VO 鍒楄〃閲嶅缓鏍戝舰缁撴瀯
        List<WorkspaceVO> tree = buildVOTree(flatVos);

        return Result.success(tree);
    }

    /**
     * 鏂板宸ヤ綔绌洪棿
     */
    @PostMapping("")
    public Result<Void> createWorkspace(@Valid @RequestBody WorkspaceRequest request) {
        log.info("鏂板宸ヤ綔绌洪棿锛宯ame: {}", request.getName());

        WorkspaceBO workspaceBO = MapstructUtils.convert(request, WorkspaceBO.class);
        boolean success = workspaceService.createWorkspace(workspaceBO);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("鏂板澶辫触");
        }
    }

    /**
     * 淇敼宸ヤ綔绌洪棿
     */
    @PatchMapping("/{id}")
    public Result<Void> updateWorkspace(
            @PathVariable Long id,
            @Valid @RequestBody WorkspaceRequest request) {
        log.info("淇敼宸ヤ綔绌洪棿锛宨d: {}", id);

        WorkspaceBO workspaceBO = MapstructUtils.convert(request, WorkspaceBO.class);
        boolean success = workspaceService.updateWorkspace(id, workspaceBO);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("淇敼澶辫触");
        }
    }

    /**
     * 鍒犻櫎宸ヤ綔绌洪棿
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteWorkspace(@PathVariable Long id) {
        log.info("鍒犻櫎宸ヤ綔绌洪棿锛宨d: {}", id);

        boolean success = workspaceService.deleteWorkspace(id);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("鍒犻櫎澶辫触锛屽彲鑳藉瓨鍦ㄥ瓙宸ヤ綔绌洪棿");
        }
    }

    /**
     * 灏嗘爲褰?BO 鍒楄〃骞抽摵涓烘墎骞冲垪琛紝鍚屾椂娓呴櫎 children 浠ラ伩鍏嶉€掑綊杞崲
     */
    private List<WorkspaceBO> flattenBos(List<WorkspaceBO> bos) {
        List<WorkspaceBO> flat = new ArrayList<>();
        flattenBosRecursive(bos, flat);
        return flat;
    }

    private void flattenBosRecursive(List<WorkspaceBO> nodes, List<WorkspaceBO> result) {
        if (nodes == null) {
            return;
        }
        for (WorkspaceBO node : nodes) {
            // 鍏堜繚瀛?children 寮曠敤锛屽啀娓呯┖
            List<WorkspaceBO> children = node.getChildren();
            node.setChildren(null);
            result.add(node);
            // 閫掑綊澶勭悊瀛愯妭鐐?
            if (children != null) {
                flattenBosRecursive(children, result);
            }
        }
    }

    /**
     * 浠庢墎骞?VO 鍒楄〃閲嶅缓鏍戝舰缁撴瀯锛堝熀浜?parentId 瀛楁锛夛紝
     * 鐖惰妭鐐逛负 0L 鎴?null 鐨勪綔涓烘牴鑺傜偣
     */
    private List<WorkspaceVO> buildVOTree(List<WorkspaceVO> flatVos) {
        if (flatVos == null || flatVos.isEmpty()) {
            return new ArrayList<>();
        }

        // 寤虹珛 id -> VO 鏄犲皠
        Map<Long, WorkspaceVO> voMap = new HashMap<>();
        for (WorkspaceVO vo : flatVos) {
            vo.setChildren(new ArrayList<>());
            voMap.put(vo.getId(), vo);
        }

        // 鎸?parentId 鎸傝浇瀛愯妭鐐癸紝鍚屾椂鏀堕泦鏍硅妭鐐?
        List<WorkspaceVO> roots = new ArrayList<>();
        for (WorkspaceVO vo : flatVos) {
            Long parentId = vo.getParentId();
            if (parentId == null || parentId == 0L) {
                roots.add(vo);
            } else {
                WorkspaceVO parent = voMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // parentId 鎸囧悜涓嶅瓨鍦ㄧ殑鑺傜偣锛岄檷绾т负鏍硅妭鐐?
                    log.warn("宸ヤ綔绌洪棿 {} 鐨?parentId={} 涓嶅瓨鍦紝闄嶇骇涓烘牴鑺傜偣", vo.getId(), parentId);
                    roots.add(vo);
                }
            }
        }

        return roots;
    }

}
