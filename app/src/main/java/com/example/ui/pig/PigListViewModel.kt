package com.example.ui.pig

import com.example.domain.repository.PigRepository
import com.example.domain.repository.SystemParameterRepository
import com.example.domain.rule.FarmRuleEngine
import com.example.domain.usecase.AddPigUseCase
import com.example.domain.usecase.DeletePigUseCase
import com.example.domain.usecase.GetPigByIdUseCase
import com.example.domain.usecase.GetPigStatsUseCase
import com.example.domain.usecase.GetPigsUseCase
import com.example.domain.usecase.UpdatePigUseCase
import kotlinx.coroutines.flow.StateFlow

/**
 * PigListViewModel quản lý trạng thái dữ liệu (StateFlow) của danh sách đàn heo
 * lấy trực tiếp từ PigRepository và phản ánh lên giao diện Jetpack Compose theo kiến trúc MVVM.
 *
 * Tính năng chính:
 * - Quan sát danh sách đàn heo thời gian thực qua StateFlow (uiState).
 * - Quản lý tìm kiếm mã số tai (Tag ID), mã lô và chuồng trại.
 * - Quản lý bộ lọc đa tiêu chí: Tất cả, Theo Lô, Cá thể nái, Cần kiểm tra vắc-xin/siêu âm.
 * - Sắp xếp linh hoạt theo Mã tai hoặc Ngày sinh (Tăng / Giảm dần).
 * - Cập nhật số lượng đầu con thực tế và cơ cấu đàn.
 * - Phản ánh tức thì các sự kiện nhập heo, chuyển chuồng, cân định kỳ, loại thải lên UI Compose.
 */
class PigListViewModel(
    getPigsUseCase: GetPigsUseCase,
    getPigByIdUseCase: GetPigByIdUseCase,
    addPigUseCase: AddPigUseCase,
    updatePigUseCase: UpdatePigUseCase,
    deletePigUseCase: DeletePigUseCase,
    getPigStatsUseCase: GetPigStatsUseCase,
    farmRuleEngine: FarmRuleEngine? = null,
    systemParameterRepository: SystemParameterRepository? = null
) : PigViewModel(
    getPigsUseCase = getPigsUseCase,
    getPigByIdUseCase = getPigByIdUseCase,
    addPigUseCase = addPigUseCase,
    updatePigUseCase = updatePigUseCase,
    deletePigUseCase = deletePigUseCase,
    getPigStatsUseCase = getPigStatsUseCase,
    farmRuleEngine = farmRuleEngine,
    systemParameterRepository = systemParameterRepository
) {
    /**
     * Constructor tiện lợi khởi tạo trực tiếp từ PigRepository theo chuẩn Constructor Injection.
     */
    constructor(
        pigRepository: PigRepository,
        farmRuleEngine: FarmRuleEngine? = null,
        systemParameterRepository: SystemParameterRepository? = null
    ) : this(
        getPigsUseCase = GetPigsUseCase(pigRepository),
        getPigByIdUseCase = GetPigByIdUseCase(pigRepository),
        addPigUseCase = AddPigUseCase(pigRepository),
        updatePigUseCase = UpdatePigUseCase(pigRepository),
        deletePigUseCase = DeletePigUseCase(pigRepository),
        getPigStatsUseCase = GetPigStatsUseCase(pigRepository),
        farmRuleEngine = farmRuleEngine,
        systemParameterRepository = systemParameterRepository
    )

    /**
     * Alias trạng thái danh sách heo phục vụ giao diện Compose
     */
    val pigListState: StateFlow<PigUiState>
        get() = uiState
}
