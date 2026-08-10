package com.likelion.olion.domain.mate.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.mate.dto.MatePinRequest;
import com.likelion.olion.domain.mate.dto.MatePinResponse;
import com.likelion.olion.domain.mate.dto.MatePinSaveResponse;
import com.likelion.olion.domain.mate.entity.MatePin;
import com.likelion.olion.domain.mate.repository.MatePinRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatePinService {
    private static final int MAX_PIN_COUNT = 5;

    private final MatePinRepository matePinRepository;
    private final UserBookRepository userBookRepository;

    public MatePinService(MatePinRepository matePinRepository, UserBookRepository userBookRepository) {
        this.matePinRepository = matePinRepository;
        this.userBookRepository = userBookRepository;
    }

    public MatePinResponse getPins(Long userId) {
        return MatePinResponse.from(matePinRepository.findByUserIdOrderByPinnedOrderAsc(userId));
    }

    @Transactional
    public MatePinSaveResponse addPin(Long userId, MatePinRequest request) {
        UserBook userBook = userBookRepository.findByUserBookIdAndUserId(request.userBookId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (matePinRepository.existsByUserIdAndUserBookUserBookId(userId, request.userBookId())
                || matePinRepository.countByUserId(userId) >= MAX_PIN_COUNT) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        int pinnedOrder = findNextOrder(userId);
        matePinRepository.save(new MatePin(userId, userBook, pinnedOrder));
        return new MatePinSaveResponse(pinnedOrder);
    }

    @Transactional
    public void removePin(Long userId, Long userBookId) {
        MatePin pin = matePinRepository.findByUserIdAndUserBookUserBookId(userId, userBookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        matePinRepository.delete(pin);
    }

    private int findNextOrder(Long userId) {
        return matePinRepository.findByUserIdOrderByPinnedOrderAsc(userId).stream()
                .mapToInt(MatePin::getPinnedOrder)
                .filter(order -> order > 0 && order <= MAX_PIN_COUNT)
                .sorted()
                .reduce(1, (next, order) -> next == order ? next + 1 : next);
    }
}
