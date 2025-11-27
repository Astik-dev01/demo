package kg.taskflow.mapper;

import kg.taskflow.db.entity.Notification;
import kg.taskflow.dto.notification.NotificationDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(Notification notification);

    List<NotificationDto> toDtoList(List<Notification> notifications);
}
