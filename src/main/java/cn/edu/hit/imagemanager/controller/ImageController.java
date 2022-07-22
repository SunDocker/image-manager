package cn.edu.hit.imagemanager.controller;

import cn.edu.hit.imagemanager.util.ImageManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("hitminer")
public class ImageController {

    // TODO 请求类型？
    @GetMapping("{imageName}")
    public String loadImage(@PathVariable String imageName) {
        // TODO 参数检查？
        return ImageManager.loadImage(imageName);
    }

}
