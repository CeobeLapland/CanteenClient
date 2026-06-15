package ceobe.canteenclient.entity;

import ceobe.canteenclient.net.dto.Dtos;
import ceobe.canteenclient.net.request.CreateFoodRequest;
//import cn.hutool.core.collection.CollUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO <--> 实体Item 转换工具类
 * 字段基本一致，嵌套对象递归转换，空值安全
 */
public class ConvertUtil {
    
    /*/ region User 转换
    public static Dtos.UserDto itemToDto(UserItem item) {
        if (item == null) {
            return null;
        }
        Dtos.UserDto dto = new Dtos.UserDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPermission(item.getPermission());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    public static UserItem dtoToItem(Dtos.UserDto dto) {
        if (dto == null) {
            return null;
        }
        UserItem item = new UserItem();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setPermission(dto.getPermission());
        item.setCreatedAt(dto.getCreatedAt());
        return item;
    }

    public static List<Dtos.UserDto> itemListToDtoList(List<UserItem> itemList) {
        if (CollUtil.isEmpty(itemList)) {
            return new ArrayList<>();
        }
        return itemList.stream().map(ConvertUtil::itemToDto).collect(Collectors.toList());
    }

    public static List<UserItem> dtoListToItemList(List<Dtos.UserDto> dtoList) {
        if (CollUtil.isEmpty(dtoList)) {
            return new ArrayList<>();
        }
        return dtoList.stream().map(ConvertUtil::dtoToItem).collect(Collectors.toList());
    }
    // endregion*/
    
    // region FoodSummary 转换
    public static Dtos.FoodSummaryDto itemToSummaryDto(FoodItem item) {
        if (item == null) {
            return null;
        }
        Dtos.FoodSummaryDto dto = new Dtos.FoodSummaryDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setCampus(item.getCampus());
        dto.setCanteen(item.getCanteen());
        dto.setAverageRating((float) item.getScore());
        return dto;
    }

    public static FoodItem dtoToItem(Dtos.FoodSummaryDto dto) {
        if (dto == null) {
            return null;
        }
        FoodItem item = new FoodItem();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        item.setCampus(dto.getCampus());
        item.setCanteen(dto.getCanteen());
        item.setScore(dto.getAverageRating());
        return item;
    }

    public static List<Dtos.FoodSummaryDto> itemListToSummaryDtoList(List<FoodItem> itemList) {
        //if (CollUtil.isEmpty(itemList))
        if(itemList == null || itemList.isEmpty())
            return new ArrayList<>();
        return itemList.stream().map(ConvertUtil::itemToSummaryDto).collect(Collectors.toList());
    }

    public static List<FoodItem> dtoListToItemList(List<Dtos.FoodSummaryDto> dtoList) {
        //if (CollUtil.isEmpty(dtoList))
        if(dtoList == null || dtoList.isEmpty())
            return new ArrayList<>();
        return dtoList.stream().map(ConvertUtil::dtoToItem).collect(Collectors.toList());
    }
    // endregion

    // region FoodDetail 转换
    public static Dtos.FoodDetailDto itemToDetailDto(FoodItem item) {
        if (item == null) {
            return null;
        }
        Dtos.FoodDetailDto dto = new Dtos.FoodDetailDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setCampus(item.getCampus());
        dto.setCanteen(item.getCanteen());
        dto.setFloor(item.getFloor());
        dto.setWindow(item.getWindow());
        dto.setSellTime(item.getSellTime());
        dto.setTags(item.getTags());
        dto.setAverageRating((float) item.getScore());
        dto.setRatingCount(item.getRatingCount());
        return dto;
    }

    public static FoodItem dtoToItem(Dtos.FoodDetailDto dto) {
        if (dto == null) {
            return null;
        }
        FoodItem item = new FoodItem();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCreatedAt(dto.getCreatedAt());
        item.setUpdatedAt(dto.getUpdatedAt());
        item.setCampus(dto.getCampus());
        item.setCanteen(dto.getCanteen());
        item.setFloor(dto.getFloor());
        item.setWindow(dto.getWindow());
        item.setSellTime(dto.getSellTime());
        item.setTags(dto.getTags());
        //item.setScore(dto.getAverageRating());
        //item.setRatingCount(dto.getRatingCount());
        return item;
    }

    public static List<Dtos.FoodDetailDto> itemListToDtoList(List<FoodItem> itemList) {
        //if (CollUtil.isEmpty(itemList))
        if(itemList == null || itemList.isEmpty())
            return new ArrayList<>();
        return itemList.stream().map(ConvertUtil::itemToDetailDto).collect(Collectors.toList());
    }

    public static List<CreateFoodRequest> itemListToCreateRequestList(List<FoodItem> itemList) {
        if (itemList == null || itemList.isEmpty())
            return new ArrayList<>();
        return itemList.stream().map(item -> {
            CreateFoodRequest req = new CreateFoodRequest();
            req.setName(item.getName());
            req.setDescription(item.getDescription());
            req.setPrice(item.getPrice());
            req.setCampusName(item.getCampus());
            req.setCanteenName(item.getCanteen());
            req.setFloorName(item.getFloor());
            req.setWindowName(item.getWindow());
            req.setSellTime(item.getSellTime());
            req.setTags(item.getTags());
            return req;
        }).collect(Collectors.toList());
    }
    // endregion

    /*/ region Comment 转换（嵌套 User）
    public static Dtos.CommentDto itemToDto(CommentItem item) {
        if (item == null) {
            return null;
        }
        Dtos.CommentDto dto = new Dtos.CommentDto();
        dto.setId(item.getId());
        dto.setContent(item.getContent());
        dto.setAuthor(itemToDto(item.getAuthor()));
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    public static CommentItem dtoToItem(Dtos.CommentDto dto) {
        if (dto == null) {
            return null;
        }
        CommentItem item = new CommentItem();
        item.setId(dto.getId());
        item.setContent(dto.getContent());
        item.setAuthor(dtoToItem(dto.getAuthor()));
        item.setCreatedAt(dto.getCreatedAt());
        return item;
    }

    public static List<Dtos.CommentDto> itemListToDtoList(List<CommentItem> itemList) {
        if (CollUtil.isEmpty(itemList)) {
            return new ArrayList<>();
        }
        return itemList.stream().map(ConvertUtil::itemToDto).collect(Collectors.toList());
    }
    // endregion*/

    /*/ region PostSummary 转换（嵌套 User、List<String>）
    public static Dtos.PostSummaryDto itemToDto(PostSummaryItem item) {
        if (item == null) {
            return null;
        }
        Dtos.PostSummaryDto dto = new Dtos.PostSummaryDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setViewCount(item.getViewCount());
        dto.setLikeCount(item.getLikeCount());
        dto.setAuthor(itemToDto(item.getAuthor()));
        dto.setFoods(item.getFoods());
        dto.setCommentCount(item.getCommentCount());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    public static PostSummaryItem dtoToItem(Dtos.PostSummaryDto dto) {
        if (dto == null) {
            return null;
        }
        PostSummaryItem item = new PostSummaryItem();
        item.setId(dto.getId());
        item.setTitle(dto.getTitle());
        item.setViewCount(dto.getViewCount());
        item.setLikeCount(dto.getLikeCount());
        item.setAuthor(dtoToItem(dto.getAuthor()));
        item.setFoods(dto.getFoods());
        item.setCommentCount(dto.getCommentCount());
        item.setCreatedAt(dto.getCreatedAt());
        return item;
    }
    // endregion*/

    /*/ region PostDetail 转换（多层嵌套）
    public static Dtos.PostDetailDto itemToDto(PostDetailItem item) {
        if (item == null) {
            return null;
        }
        Dtos.PostDetailDto dto = new Dtos.PostDetailDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setContent(item.getContent());
        dto.setViewCount(item.getViewCount());
        dto.setLikeCount(item.getLikeCount());
        dto.setAuthor(itemToDto(item.getAuthor()));

        // Set<FoodItem> -> Set<FoodSummaryDto>
        if (item.getFoods() != null) {
            dto.setFoods(item.getFoods().stream()
                    .map(ConvertUtil::itemToDto)
                    .collect(Collectors.toSet()));
        }
        // List<CommentItem> -> List<CommentDto>
        dto.setComments(itemListToDtoList(item.getComments()));

        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    public static PostDetailItem dtoToItem(Dtos.PostDetailDto dto) {
        if (dto == null) {
            return null;
        }
        PostDetailItem item = new PostDetailItem();
        item.setId(dto.getId());
        item.setTitle(dto.getTitle());
        item.setContent(dto.getContent());
        item.setViewCount(dto.getViewCount());
        item.setLikeCount(dto.getLikeCount());
        item.setAuthor(dtoToItem(dto.getAuthor()));

        // Set<FoodSummaryDto> -> Set<FoodItem>
        if (dto.getFoods() != null) {
            item.setFoods(dto.getFoods().stream()
                    .map(ConvertUtil::dtoToItem)
                    .collect(Collectors.toSet()));
        }
        // List<CommentDto> -> List<CommentItem>
        item.setComments(dtoListToItemList(dto.getComments()));

        item.setCreatedAt(dto.getCreatedAt());
        item.setUpdatedAt(dto.getUpdatedAt());
        return item;
    }
    // endregion*/

    /*/ region WindowSearch 转换
    public static Dtos.WindowSearchDto itemToDto(WindowSearchItem item) {
        if (item == null) {
            return null;
        }
        Dtos.WindowSearchDto dto = new Dtos.WindowSearchDto();
        dto.setName(item.getName());
        dto.setFloor(item.getFloor());
        dto.setCanteen(item.getCanteen());
        dto.setCampus(item.getCampus());
        return dto;
    }

    public static WindowSearchItem dtoToItem(Dtos.WindowSearchDto dto) {
        if (dto == null) {
            return null;
        }
        WindowSearchItem item = new WindowSearchItem();
        item.setName(dto.getName());
        item.setFloor(dto.getFloor());
        item.setCanteen(dto.getCanteen());
        item.setCampus(dto.getCampus());
        return item;
    }
    // endregion*/

    /*/ region Tag 转换
    public static Dtos.TagDto itemToDto(TagItem item) {
        if (item == null) {
            return null;
        }
        Dtos.TagDto dto = new Dtos.TagDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        return dto;
    }

    public static TagItem dtoToItem(Dtos.TagDto dto) {
        if (dto == null) {
            return null;
        }
        TagItem item = new TagItem();
        item.setId(dto.getId());
        item.setName(dto.getName());
        return item;
    }
    // endregion*/

    /*/ region Seasoning 转换
    public static Dtos.SeasoningDto itemToDto(SeasoningItem item) {
        if (item == null) {
            return null;
        }
        Dtos.SeasoningDto dto = new Dtos.SeasoningDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        return dto;
    }

    public static SeasoningItem dtoToItem(Dtos.SeasoningDto dto) {
        if (dto == null) {
            return null;
        }
        SeasoningItem item = new SeasoningItem();
        item.setId(dto.getId());
        item.setName(dto.getName());
        return item;
    }
    // endregion*/
}