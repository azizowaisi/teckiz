package com.teckiz.repository;

import com.teckiz.entity.WebWidget;
import com.teckiz.entity.WidgetContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WidgetContentRepository extends JpaRepository<WidgetContent, Long> {

    Optional<WidgetContent> findByContentKey(String contentKey);

    List<WidgetContent> findByWidget(WebWidget widget);

    List<WidgetContent> findByWidgetAndActiveTrueOrderByPositionAsc(WebWidget widget);
}

