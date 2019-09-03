#ifndef __BTD_2D_H__
#define __BTD_2D_H__
#include <cmath>
#include <iostream>
#include <map>
#include <memory>
#include <random>

namespace Touch {

const float PI = std::atan(1.0) * 4;

template <class T> // int for px, float for mm
struct Point {
    T x;
    T y;

    explicit Point(T x = 0, T y = 0)
        : x(x)
        , y(y)
    {
    }
};

template <class T>
std::ostream& operator<<(std::ostream& os, const Touch::Point<T> pt)
{
    os << '(' << pt.x << ',' << pt.y << ')';
    return os;
}

class Btd2D {
public:
    enum FingerType {
        NORMAL,
        THUMB,
        INDEX
    };

    Btd2D(float diameter, FingerType finger = NORMAL, bool is_verbose = false);

    float calcBTD2D(Point<float> s, Point<float> t);

    float calcLogLikeli(Point<float> s, Point<float> t)
    {
        return -1 * (calcBTD2D(s, t) + 0.5 * std::log(2 * PI)) * std::log10(std::exp(1));
    }

private:
    Btd2D() {} // Block calling default constructor.
    float m_diameter_;
    FingerType m_finger_;
    bool m_is_verbose_;
    float m_alpha_x_, m_sigma_a_x_2_, m_alpha_y_, m_sigma_a_y_2_;
    float m_tmp_x_, m_tmp_y_;
};
class Keyboard {
public:
    Keyboard(std::string name = "iPhone5s");
    Keyboard(float btn_w, float btn_h, float left_margin[3], int dpi);
    ~Keyboard()
    {
        delete mp_nd_x_;
        delete mp_nd_y_;
        delete mp_gen;
    }

    Point<int> getPositionPX(char key) { return m_layout_px_[key]; }
    Point<float> getPositionMM(char key) { return m_layout_mm_[key]; }
    char getShortestKey(Point<float> p);
    char getShortestKey(Point<int> p)
    {
        return getShortestKey(Point<float>((float) p.x / m_pixels_to_mm_, (float) p.y / m_pixels_to_mm_));
    }
    float getDistance(Point<int> p1, Point<int> p2)
    {
        Point<float> p1_((float)p1.x / m_pixels_to_mm_, (float)p1.y / m_pixels_to_mm_);
        Point<float> p2_((float)p2.x / m_pixels_to_mm_, (float)p2.y / m_pixels_to_mm_);

        return getDistance(p1_, p2_);
    }
    float getDistance(Point<float> p1, Point<float> p2)
    {
        if(mp_btd2d == nullptr)
            return std::sqrt(std::pow(p2.x - p1.x, 2) + std::pow(p2.y - p1.y, 2));
        else
            return mp_btd2d->calcBTD2D(p2, p1);
    }
    float getDistance(char key, Point<float> p2)
    {
        Point<float> p1 = getPositionMM(key);
        return getDistance(p1, p2);
    }
    Point<float> generateTouchPoint(char key);
    float getDiameter() { return m_diameter_; }
    float pixelsToMM(int pixel) { return (float)pixel / m_pixels_to_mm_; }
    int MMToPixels(float mm) { return (int)(mm * m_pixels_to_mm_); }
    void setBtd2D(std::shared_ptr<Btd2D> pbtd2d) { mp_btd2d = pbtd2d; }

private:
    static const std::string QwertySequence[];
    std::map<char, Point<int>> m_layout_px_;
    std::map<char, Point<float>> m_layout_mm_;
    float m_pixels_to_mm_;
    std::string m_keyboard_name_;
    float m_diameter_;
    std::normal_distribution<float>*mp_nd_x_, *mp_nd_y_;
    std::random_device m_rd;
    std::mt19937* mp_gen;
    int m_btn_w, m_btn_h, m_tap_x, m_tap_y, m_left_margin[3], m_top_margin;
    std::shared_ptr<Btd2D> mp_btd2d;

    void setupKeyboard();
};

}
#endif
