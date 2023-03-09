package com.example.sbapec

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.sbapec.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupView()
    }

    private fun setupView() {
        mBinding.toolbarLogin.tvTitle.text = getString(R.string.sign_in)

        mBinding.etEmail.addTextChangedListener {
            mBinding.tilEmail.isErrorEnabled = false
            mBinding.tvSignIn.isEnabled =
                mBinding.etEmail.text.toString().isNotEmpty() && mBinding.etPassword.text.toString()
                    .isNotEmpty()
        }

        mBinding.etPassword.addTextChangedListener {
            mBinding.tvSignIn.isEnabled =
                mBinding.etEmail.text.toString().isNotEmpty() && mBinding.etPassword.text.toString()
                    .isNotEmpty()
        }

        mBinding.tvSignIn.setOnClickListener {
            if (ValidationUtils.emailValidator(mBinding.etEmail.text.toString().trim()))
                startActivity(Intent(this, HomeActivity::class.java))
            else {
                mBinding.tilEmail.error = getString(R.string.invalid_email_address)
                mBinding.tvSignIn.isEnabled = false
            }
        }
    }
}