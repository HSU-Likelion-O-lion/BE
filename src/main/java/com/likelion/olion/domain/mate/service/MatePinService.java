package com.likelion.olion.domain.mate.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.mate.dto.MatePinRequest;
import com.likelion.olion.domain.mate.dto.MatePinResponse;
import com.likelion.olion.domain.mate.dto.MatePinSaveResponse;
import com.likelion.olion.domain.mate.entity.MatePin;
import com.likelion.olion.domain.mate.repository.MatePinRepository;
import com.likelion.olion.domain.user.entity.SubscriptionPlan;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatePinService {
    private final MatePinRepository matePinRepository;
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;

    public MatePinService(
            MatePinRepository matePinRepository,
            UserBookRepository userBookRepository,
            UserRepository userRepository
    ) {
        this.matePinRepository = matePinRepository;
        this.userBookRepository = userBookRepository;
        this.userRepository = userRepository;
    }

    public MatePinResponse getPins(Long userId) {
        return MatePinResponse.from(matePinRepository.findByUserIdOrderByPinnedOrderAsc(userId));
    }

    @Transactional
    public MatePinSaveResponse addPin(Long userId, MatePinRequest request) {
        UserBook userBook = userBookRepository.findByUserBookIdAndUserId(request.userBookId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        int maxPinCount = maxPinCount(userId);
        if (matePinRepository.existsByUserIdAndUserBookUserBookId(userId, request.userBookId())
                || matePinRepository.countByUserId(userId) >= maxPinCount) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        int pinnedOrder = findNextOrder(userId, maxPinCount);
        matePinRepository.save(new MatePin(userId, userBook, pinnedOrder));
        return new MatePinSaveResponse(pinnedOrder);
    }

    @Transactional
    public void removePin(Long userId, Long userBookId) {
        MatePin pin = matePinRepository.findByUserIdAndUserBookUserBookId(userId, userBookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        matePinRepository.delete(pin);
    }

    private int maxPinCount(Long userId) {
        return userRepository.findById(userId)
                .map(User::getPlan)
                .orElse(SubscriptionPlan.BASIC)
                .maxMatePinCount();
    }

    private int findNextOrder(Long userId, int maxPinCount) {
        return matePinRepository.findByUserIdOrderByPinnedOrderAsc(userId).stream()
                .mapToInt(MatePin::getPinnedOrder)
                .filter(order -> order > 0 && order <= maxPinCount)
                .sorted()
                .reduce(1, (next, order) -> next == order ? next + 1 : next);
    }
}
