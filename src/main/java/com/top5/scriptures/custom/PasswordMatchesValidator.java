package com.top5.scriptures.custom;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.top5.scriptures.model.User;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

	@Override
	public void initialize(PasswordMatches constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object obj, ConstraintValidatorContext context) {
		User user = (User) obj;
		boolean isValid = user.getPassword().equals(user.getMatchingPassword());
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
					.addPropertyNode("matchingPassword").addConstraintViolation();
		}
		return isValid;
	}
}
