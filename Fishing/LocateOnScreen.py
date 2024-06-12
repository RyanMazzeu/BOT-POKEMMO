from pyautogui import screenshot
import cv2
from cv2 import (
    imread,
    IMREAD_GRAYSCALE,
    cvtColor,
    COLOR_RGB2GRAY,
    resize,
    INTER_LINEAR,
    matchTemplate,
    TM_CCOEFF_NORMED,
    minMaxLoc,
    destroyAllWindows,
)
from sys import argv
import numpy as np


def locate_scaled_image(image_path, region=None, base_scale=1, confidence=0.6):
    try:
        original_image = imread(image_path, IMREAD_GRAYSCALE)
        if original_image is None:
            print("Unable to load the image.")
            return

        # Capture the screen directly in grayscale
        screen = screenshot(region=region)
        screen_gray = cvtColor(np.array(screen), COLOR_RGB2GRAY)

        scale_steps = [0, 5, -5, 10, -10, 15, -15, 20, -20]

        for step in scale_steps:
            scale = base_scale + step / 100.0
            scaled_image = resize(
                original_image, None, fx=scale, fy=scale, interpolation=INTER_LINEAR
            )

            if (
                scaled_image.shape[0] > screen_gray.shape[0]
                or scaled_image.shape[1] > screen_gray.shape[1]
            ):
                print(
                    f"Skipped scale {scale}: Scaled image is larger than the screen capture."
                )
                continue

            result = matchTemplate(screen_gray, scaled_image, TM_CCOEFF_NORMED)
            min_val, max_val, min_loc, max_loc = minMaxLoc(result)

            if max_val >= confidence:
                top_left = max_loc
                h, w = scaled_image.shape[:2]
                bottom_right = (top_left[0] + w, top_left[1] + h)
                print(f"{top_left[0]},{top_left[1]},{w},{h}")
                return top_left, bottom_right

        print("Image not found.")
    except Exception as e:
        print(f"An error occurred: {e}")
    finally:
        destroyAllWindows()
        try:
            del original_image, screen_gray, scaled_image, result
        except NameError:
            pass


if __name__ == "__main__":
    if len(argv) != 6:
        print("Usage: python LocateOnScreen.py <image_path> <x> <y> <width> <height>")
    else:
        image_path = argv[1]
        region = tuple(map(int, argv[2:6]))
        locate_scaled_image(image_path, region)
