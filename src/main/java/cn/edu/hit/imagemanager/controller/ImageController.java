package cn.edu.hit.imagemanager.controller;

import cn.edu.hit.imagemanager.util.ImageManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hitminer")
public class ImageController {

    // TODO 请求类型？
    @PutMapping("{imageName}")
    public String loadImage(@PathVariable String imageName) {
        return ImageManager.loadImage(imageName);
    }

}
