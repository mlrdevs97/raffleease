package com.raffleease.raffleease.Domains.Images.Repository;

import com.raffleease.raffleease.Domains.Images.Model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("ImagesRepository")
public interface ImagesRepository extends JpaRepository<Image, Long> {
}
